package org.example.scheduler;

import java.time.LocalDateTime;

import org.example.entity.PaymentStatus;
import org.example.entity.VoucherStatus;
import org.example.repository.PurchaseRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PurchaseExpirationScheduler {

    private final PurchaseRepository purchaseRepository;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void expirePendingPurchases() {

        LocalDateTime now = LocalDateTime.now();
        var toExpire = purchaseRepository.findToExpire(now);

        for (var purchase : toExpire) {

            if (purchase.getVoucherStatus() != null)
                continue;

            var voucher = purchase.getVoucher();
            var payment = purchase.getPayment();

            purchase.setVoucherStatus(VoucherStatus.EXPIRED);
            purchase.setExpiredAt(now);

            voucher.setStock(voucher.getStock() + 1);

            // NO MARCAR REJECTED
            // Simplemente no se pagó y expiró antes.
            if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
                payment.setUpdatedAt(now);
            }
        }
    }
}
