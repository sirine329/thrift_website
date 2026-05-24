package com.thriftby.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commandes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", unique = true, nullable = false)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acheteur_id", nullable = false)
    private User acheteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendeur_id", nullable = false)
    private User vendeur;

    @Column(name = "montant_item", precision = 10, scale = 2)
    private BigDecimal montantItem;

    @Column(name = "frais_plateforme", precision = 10, scale = 2)
    private BigDecimal fraisPlateforme;

    @Column(name = "frais_livraison", precision = 10, scale = 2)
    private BigDecimal fraisLivraison;

    @Column(name = "montant_total", precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", nullable = false)
    @Builder.Default
    private ModePaiement modePaiement = ModePaiement.ESPECE_EN_LIVRAISON;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @Column(name = "stripe_session_id")
    private String stripeSessionId;

    @Column(name = "stripe_payment_id")
    private String stripePaymentId;

    @Column(name = "adresse_livraison", length = 500)
    private String adresseLivraison;

    @Column(name = "numero_suivi")
    private String numeroSuivi;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isPayee() {
        return statut == StatutCommande.PAYEE
                || statut == StatutCommande.EXPEDIEE
                || statut == StatutCommande.LIVREE;
    }

    @Override
    public String toString() {
        return "Commande{id=" + id + ", numero='" + numero + "', statut=" + statut + "}";
    }
}