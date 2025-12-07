package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.example.entity.PaymentMethod;
import org.example.entity.PaymentStatus;
import org.example.entity.VoucherStatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDto {

    @JsonProperty(access = Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = Access.WRITE_ONLY)
    @NotNull(message = "voucherId es obligatorio")
    private Long voucherId;

    @JsonProperty(access = Access.WRITE_ONLY)
    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethod paymentMethod;

    @JsonProperty(access = Access.READ_ONLY)
    private VoucherStatus voucherStatus;

    @JsonProperty(access = Access.READ_ONLY)
    private String voucherDescription;

    @JsonProperty(access = Access.READ_ONLY)
    private BigDecimal amount;

    @JsonProperty(access = Access.READ_ONLY)
    private Long paymentId;

    @JsonProperty(access = Access.READ_ONLY)
    private PaymentStatus paymentStatus;

    @JsonProperty(access = Access.READ_ONLY)
    private String externalPaymentId;

    @JsonProperty(access = Access.READ_ONLY)
    private String paymentUrl;

    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime createdAt;

    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime activatedAt;

    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime cancelledAt;

    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime expiredAt;

    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime expiresAt;

    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime usedAt;
}
