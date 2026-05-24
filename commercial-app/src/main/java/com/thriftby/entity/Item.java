package com.thriftby.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Column(nullable = false)
    private String titre;

    @Column(length = 2000)
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être positif")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @NotNull(message = "La taille est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Taille taille;

    @NotNull(message = "L'état est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Etat etat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Style style = Style.CASUAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutItem statut = StatutItem.DISPONIBLE;

    // Photo principale
    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

    // Photos supplémentaires réelles associées au propriétaire
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemPhoto> photos = new ArrayList<>();

    // Compteur de likes
    @Column(name = "nb_likes")
    @Builder.Default
    private Integer nbLikes = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "item_categories",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "categorie_id"))
    @Builder.Default
    private List<Categorie> categories = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendeur_id", nullable = false)
    private User vendeur;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Wishlist> wishlistEntries = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helpers
    public boolean isDisponible() {
        return statut == StatutItem.DISPONIBLE && actif;
    }

    public boolean isVendu() {
        return statut == StatutItem.VENDU;
    }

    public void addPhoto(ItemPhoto photo) {
        if (photo != null) {
            photo.setItem(this);
            photos.add(photo);
        }
    }

    public void removePhoto(ItemPhoto photo) {
        if (photo != null) {
            photos.remove(photo);
            photo.setItem(null);
        }
    }

    @Override
    public String toString() {
        return "Item{id=" + id + ", titre='" + titre + "', prix=" + prix + ", statut=" + statut + "}";
    }
}
