package org.example.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version; // optimistic locking

    @Column(nullable = false, length = 1024)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false; // soft delete

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Purchase> purchases = new ArrayList<>();

    public void addPurchase(Purchase purchase) {
        if (!purchases.contains(purchase)) {
            purchases.add(purchase);
            purchase.setVoucher(this);
        }
    }

    public void removePurchase(Purchase purchase) {
        if (purchases.remove(purchase))
            purchase.setVoucher(null);
    }

    public void clearPurchases() {
        for (Purchase p : purchases)
            p.setVoucher(null);
        purchases.clear();
    }
}