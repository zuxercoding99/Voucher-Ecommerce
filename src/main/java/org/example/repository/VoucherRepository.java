package org.example.repository;

import java.util.List;
import java.util.Optional;

import org.example.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    List<Voucher> findAllByDeletedFalse();

    Optional<Voucher> findByIdAndDeletedFalse(Long voucherId);

}
