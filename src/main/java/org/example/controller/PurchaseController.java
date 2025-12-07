package org.example.controller;

import org.example.dto.PurchaseDto;
import org.example.service.PurchaseService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public PurchaseDto create(@Valid @RequestBody PurchaseDto dto) {
        return purchaseService.create(dto);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public List<PurchaseDto> myPurchases() {
        return purchaseService.myPurchases();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/activate")
    public PurchaseDto activate(@PathVariable Long id) {
        return purchaseService.activate(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/used")
    public PurchaseDto markUsed(@PathVariable Long id) {
        return purchaseService.markUsed(id);
    }

    @PostMapping("/webhook/mp")
    public ResponseEntity<String> webhook(@RequestBody Map<String, Object> body) {
        System.out.println("Body en el controllador:");
        System.out.println(body);
        purchaseService.handleMercadoPagoWebhook(body);
        return ResponseEntity.ok("ok");
    }

}
