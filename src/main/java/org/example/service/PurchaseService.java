package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.PurchaseDto;
import org.example.entity.*;
import org.example.exception.customs.httpstatus.BadRequestException;
import org.example.exception.customs.httpstatus.ConflictException;
import org.example.exception.customs.httpstatus.ForbiddenException;
import org.example.exception.customs.httpstatus.NotFoundException;
import org.example.repository.PurchaseRepository;
import org.example.repository.VoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercadopago.resources.preference.Preference;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final VoucherRepository voucherRepository;
    private final MercadoPagoService mercadoPagoService;
    private final AuthService authService; // para obtener el current user

    /**
     * Crea una compra. Si es MERCADOPAGO genera preferencia, si es FAKE solo crea
     * la compra.
     */

    @Transactional
    public PurchaseDto create(PurchaseDto dto) {

        var user = authService.getCurrentUser();

        Voucher voucher = voucherRepository.findById(dto.getVoucherId())
                .orElseThrow(() -> new NotFoundException("Voucher no encontrado"));

        if (voucher.getStock() <= 0) {
            throw new ConflictException("Voucher sin stock");
        }

        // Crear purchase sin ID aún
        Purchase purchase = Purchase.builder()
                .user(user)
                .voucher(voucher)
                .voucherStatus(null)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        // Decrementar stock
        voucher.setStock(voucher.getStock() - 1);
        voucher.addPurchase(purchase);

        // Guardar para generar ID
        purchase = purchaseRepository.save(purchase);

        Payment payment;

        // ----------- MERCADOPAGO ------------
        if (dto.getPaymentMethod() == PaymentMethod.MERCADOPAGO) {

            Preference preference = mercadoPagoService.createPreference(
                    purchase.getId(), // AHORA SÍ tiene ID
                    voucher.getDescription(),
                    voucher.getPrice());

            System.out.println("Init point");
            System.out.println(preference.getInitPoint());

            payment = Payment.builder()
                    .purchase(purchase)
                    .amount(voucher.getPrice())
                    .method(PaymentMethod.MERCADOPAGO)
                    .status(PaymentStatus.PENDING)
                    .externalId(purchase.getId().toString())
                    .paymentUrl(preference.getInitPoint()) // URL para pagar
                    .build();

        }

        // ----------- FAKE PAYMENT ------------
        else if (dto.getPaymentMethod() == PaymentMethod.FAKE) {

            payment = Payment.builder()
                    .purchase(purchase)
                    .amount(voucher.getPrice())
                    .method(PaymentMethod.FAKE)
                    .status(PaymentStatus.APPROVED)
                    .externalId("FAKE-" + System.currentTimeMillis())
                    .build();

            purchase.setVoucherStatus(VoucherStatus.AVAILABLE);

        }

        else {
            throw new BadRequestException("Método de pago no soportado");
        }

        purchase.setPayment(payment);

        purchase = purchaseRepository.save(purchase);

        return toDto(purchase);
    }

    @Transactional(readOnly = true)
    public List<PurchaseDto> myPurchases() {

        var user = authService.getCurrentUser();

        List<Purchase> purchases = purchaseRepository.findAllByUserAndDeletedFalse(user);

        return purchases.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Maneja el webhook de MercadoPago
     */
    @Transactional
    public void handleMercadoPagoWebhook(Map<String, Object> body) {
        System.out.println("Llamada al webhook");
        System.out.println(body);

        // 1. Validar que venga el paymentId
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        if (data == null || !data.containsKey("id")) {
            System.out.println("Webhook ignorado: no trae data.id");
            return;
        }

        Long paymentId = Long.valueOf(data.get("id").toString());

        // 2. Obtener el pago REAL desde Mercado Pago
        com.mercadopago.resources.payment.Payment mpPayment = mercadoPagoService.getPayment(paymentId);

        String status = mpPayment.getStatus(); // approved, rejected, pending
        String externalRef = mpPayment.getExternalReference(); // purchaseId que mandaste

        System.out.println("STATUS: " + status);
        System.out.println("externalRef; " + externalRef);
        System.out.println("PaymentId: " + paymentId);

        if (externalRef == null) {
            System.out.println("Pago sin external_reference. Ignorado.");
            return;
        }

        Long purchaseId = Long.valueOf(externalRef);

        // 3. Obtener tu purchase y payment
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase no encontrado"));

        Payment payment = purchase.getPayment();

        // 4. Guardar el paymentId REAL si aún no lo tenías
        payment.setExternalId(paymentId.toString());
        payment.setUpdatedAt(LocalDateTime.now());

        // 5. Procesar segun estado
        switch (status) {

            case "approved" -> {
                // Si ya está aprobado no repetir (idempotencia)
                if (payment.getStatus() == PaymentStatus.APPROVED)
                    return;

                if (purchase.getVoucherStatus() == VoucherStatus.EXPIRED) {
                    payment.setStatus(PaymentStatus.PENDING_REFUND);
                    payment.setUpdatedAt(LocalDateTime.now());
                    return;
                }

                payment.setStatus(PaymentStatus.APPROVED);
                purchase.setVoucherStatus(VoucherStatus.AVAILABLE);
            }

            case "rejected" -> {
                // Si ya está rejected no repetir
                if (payment.getStatus() == PaymentStatus.REJECTED)
                    return;

                payment.setStatus(PaymentStatus.REJECTED);

                // devolver stock SOLO si no está usado
                if (purchase.getVoucherStatus() == null) {
                    Voucher voucher = purchase.getVoucher();
                    voucher.setStock(voucher.getStock() + 1);
                }
            }

            case "pending" -> {
                payment.setStatus(PaymentStatus.PENDING);
            }
        }

        purchaseRepository.save(purchase);
    }

    /**
     * Activa una compra manualmente (si no estaba activada)
     */
    @Transactional
    public PurchaseDto activate(Long purchaseId) {
        var user = authService.getCurrentUser();

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase no encontrado"));

        if (!purchase.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("No sos dueño de esta compra");
        }

        if (purchase.getVoucherStatus() != VoucherStatus.AVAILABLE) {
            throw new BadRequestException("No podés activar un voucher que no está disponible");
        }

        purchase.setVoucherStatus(VoucherStatus.ACTIVATED);
        purchase.setActivatedAt(LocalDateTime.now());
        purchaseRepository.save(purchase);

        return toDto(purchase);
    }

    /**
     * Marca una compra como usada
     */
    @Transactional
    public PurchaseDto markUsed(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase no encontrado"));

        if (purchase.getVoucherStatus() != VoucherStatus.ACTIVATED) {
            throw new BadRequestException("Sólo se puede marcar como usado un voucher activado");
        }

        purchase.setVoucherStatus(VoucherStatus.USED);
        purchase.setUsedAt(LocalDateTime.now());
        purchaseRepository.save(purchase);

        return toDto(purchase);
    }

    /**
     * Mapeo de entidad a DTO
     */
    private PurchaseDto toDto(Purchase purchase) {
        Payment payment = purchase.getPayment();

        return PurchaseDto.builder()
                .id(purchase.getId())
                .voucherStatus(purchase.getVoucherStatus())
                .voucherDescription(purchase.getVoucher().getDescription())
                .amount(payment != null ? payment.getAmount() : null)
                .paymentId(payment != null ? payment.getId() : null)
                .paymentStatus(payment != null ? payment.getStatus() : null)
                .externalPaymentId(payment != null ? payment.getExternalId() : null)
                .paymentUrl(payment != null ? payment.getPaymentUrl() : null) // <-- aquí
                .createdAt(purchase.getCreatedAt())
                .activatedAt(purchase.getActivatedAt())
                .cancelledAt(purchase.getCancelledAt())
                .expiredAt(purchase.getExpiredAt())
                .expiresAt(purchase.getExpiresAt())
                .usedAt(purchase.getUsedAt())
                .build();
    }

}
