package com.thriftby.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false, unique = true)
    private String nom;

    @Column(length = 500)
    private String description;

    // Icône / emoji pour l'affichage (ex: "👗", "👟", "🧥")
    @Column(name = "icone")
    private String icone;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "couleur_hex")
    @Builder.Default
    private String couleurHex = "#EEEDFE";

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Item> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public long getNbItems() {
        return items != null ? items.stream().filter(Item::isDisponible).count() : 0;
    }

    @Override
    public String toString() {
        return "Categorie{id=" + id + ", nom='" + nom + "'}";
    }
}
