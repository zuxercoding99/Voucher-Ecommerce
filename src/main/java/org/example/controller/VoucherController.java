package org.example.controller;

import java.util.List;

import org.example.dto.VoucherDto;
import org.example.service.VoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public List<VoucherDto> getAllActive() {
        return voucherService.getAllActive();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public VoucherDto create(@RequestBody VoucherDto dto) {
        return voucherService.create(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        voucherService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
