package org.example.entity;

public enum PaymentStatus {
    PENDING,
    APPROVED,
    REJECTED,
    PENDING_REFUND, // pago aprobado pero compra no entregable
    REFUNDED // refund completado

}