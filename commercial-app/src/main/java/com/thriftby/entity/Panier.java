package com.thriftby.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "paniers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Panier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "panier_items",
            joinColumns = @JoinColumn(name = "panier_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    @Builder.Default
    private List<Item> items = new ArrayList<>();

    // Calcul total
    public BigDecimal getTotal() {
        return items.stream()
                .filter(Item::isDisponible)
                .map(Item::getPrix)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getFraisPlateforme() {
        return getTotal().multiply(new BigDecimal("0.05"));
    }

    public BigDecimal getFraisLivraison() {
        return items.isEmpty() ? BigDecimal.ZERO : new BigDecimal("7.00");
    }

    public BigDecimal getTotalFinal() {
        return getTotal().add(getFraisPlateforme()).add(getFraisLivraison());
    }

    public int getNbItems() {
        return items.size();
    }
}
