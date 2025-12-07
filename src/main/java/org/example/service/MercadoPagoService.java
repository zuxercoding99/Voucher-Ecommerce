package org.example.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
//import com.mercadopago.client.preference.PreferenceCreateRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MercadoPagoService {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.notification-url}")
    private String notificationUrl;

    /**
     * Crea una preferencia de pago
     */
    public Preference createPreference(Long purchaseId, String title, BigDecimal amount) {
        // Configurar token global
        MercadoPagoConfig.setAccessToken(accessToken);

        PreferenceClient client = new PreferenceClient();

        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .id(purchaseId.toString())
                .title(title)
                .quantity(1)
                .currencyId("ARS")
                .unitPrice(amount)
                .build();

        System.out.println("External reference: " + purchaseId.toString());

        PreferenceRequest request = PreferenceRequest.builder()
                .items(List.of(item))
                .notificationUrl(notificationUrl)
                .externalReference(purchaseId.toString())
                .build();

        try {
            return client.create(request); // lanza MPException, MPApiException
        } catch (MPException | MPApiException e) {
            throw new RuntimeException("Error al crear preferencia de MercadoPago", e);
        }
    }

    // consultar pago
    public Payment getPayment(Long paymentId) {
        MercadoPagoConfig.setAccessToken(accessToken);
        PaymentClient client = new PaymentClient();
        System.out.println("payment id: " + paymentId);

        try {
            return client.get(paymentId); // ahora funciona directamente
        } catch (MPException | MPApiException e) {
            throw new RuntimeException("Error al consultar pago en MercadoPago", e);
        }
    }

}
