package org.example.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.example.entity.Purchase;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findAllByUserIdAndDeletedFalse(UUID userId);

    List<Purchase> findByUserIdAndDeletedFalse(UUID userId);

    Optional<Purchase> findByIdAndDeletedFalse(Long id);

    List<Purchase> findAllByUserAndDeletedFalse(User user);

    Optional<Purchase> findByPaymentExternalId(String paymentId);

    @Query("""
             SELECT p FROM Purchase p
             WHERE p.voucherStatus IS NULL
             AND p.deleted = false
             AND p.expiresAt < :now
            """)
    List<Purchase> findToExpire(LocalDateTime now);

}
