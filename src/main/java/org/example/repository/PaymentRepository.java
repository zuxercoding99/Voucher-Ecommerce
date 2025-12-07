package org.example.repository;

import java.util.Optional;

import org.example.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByExternalIdAndDeletedFalse(String externalId);

    Optional<Payment> findByExternalId(String paymentId);
}
