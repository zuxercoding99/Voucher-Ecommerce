package org.example.service;

import java.util.List;

import org.example.dto.VoucherDto;
import org.example.entity.Voucher;
import org.example.repository.VoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepo;

    @Transactional(readOnly = true)
    public List<VoucherDto> getAllActive() {
        return voucherRepo.findAllByDeletedFalse()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public VoucherDto create(VoucherDto dto) {
        Voucher v = Voucher.builder()
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .build();
        return toDto(voucherRepo.save(v));
    }

    @Transactional
    public void softDelete(Long id) {
        voucherRepo.findById(id).ifPresent(v -> {
            v.setDeleted(true);
            voucherRepo.save(v);
        });
    }

    public VoucherDto toDto(Voucher v) {
        return VoucherDto.builder()
                .id(v.getId())
                .description(v.getDescription())
                .price(v.getPrice())
                .stock(v.getStock())
                .build();
    }
}
