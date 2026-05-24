package com.thriftby.config;

import com.thriftby.entity.*;
import com.thriftby.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository       userRepository;
    private final CategorieRepository  categorieRepository;
    private final ItemRepository       itemRepository;
    private final PasswordEncoder      passwordEncoder;

    @Override
    public void run(String... args) {
        initUsers();
        initCategories();
        initItems();
        log.info("✅ ThriftBy — données initiales chargées.");
    }

    private void initUsers() {
        if (!userRepository.existsByEmail("superadmin@thriftby.tn")) {
            userRepository.save(User.builder()
                    .nom("Admin").prenom("Super")
                    .email("superadmin@thriftby.tn")
                    .password(passwordEncoder.encode("superadmin123"))
                    .telephone("00000000").ville("Tunis")
                    .role(Role.SUPERADMIN).actif(true).verified(true).build());
            log.info("→ SuperAdmin: superadmin@thriftby.tn / superadmin123");
        }

        if (!userRepository.existsByEmail("admin@thriftby.tn")) {
            userRepository.save(User.builder()
                    .nom("Moderateur").prenom("ThriftBy")
                    .email("admin@thriftby.tn")
                    .password(passwordEncoder.encode("admin123"))
                    .telephone("11111111").ville("Tunis")
                    .role(Role.ADMIN).actif(true).verified(true).build());
            log.info("→ Admin: admin@thriftby.tn / admin123");
        }

        if (!userRepository.existsByEmail("user@thriftby.tn")) {
            userRepository.save(User.builder()
                    .nom("Mansouri").prenom("Karim")
                    .email("user@thriftby.tn")
                    .password(passwordEncoder.encode("user123"))
                    .telephone("33333333").ville("Sfax")
                    .role(Role.USER).actif(true).verified(true).build());
            log.info("→ Utilisateur: user@thriftby.tn / user123");
        }
    }

    private void initCategories() {
        if (categorieRepository.count() > 0) return;

        categorieRepository.save(Categorie.builder()
                .nom("Hauts").description("T-shirts, chemises, pulls, blouses")
                .icone("👕").couleurHex("#E6F1FB").actif(true).build());
        categorieRepository.save(Categorie.builder()
                .nom("Bas").description("Pantalons, jeans, jupes, shorts")
                .icone("👖").couleurHex("#FBEAF0").actif(true).build());
        categorieRepository.save(Categorie.builder()
                .nom("Robes").description("Robes décontractées, formelles et vintage")
                .icone("👗").couleurHex("#EEEDFE").actif(true).build());
        categorieRepository.save(Categorie.builder()
                .nom("Manteaux & Vestes").description("Vestes, manteaux, pardessus")
                .icone("🧥").couleurHex("#EAF3DE").actif(true).build());
        categorieRepository.save(Categorie.builder()
                .nom("Chaussures").description("Sneakers, boots, sandales, talons")
                .icone("👟").couleurHex("#FAEEDA").actif(true).build());
        categorieRepository.save(Categorie.builder()
                .nom("Accessoires").description("Sacs, ceintures, chapeaux, bijoux")
                .icone("👜").couleurHex("#FAECE7").actif(true).build());
    }

    private void initItems() {
        if (itemRepository.count() > 0) return;

        // L'admin sera le vendeur des articles de test
        User admin = userRepository.findByEmail("admin@thriftby.tn").orElseThrow();
        Categorie manteaux  = categorieRepository.findByNom("Manteaux & Vestes").orElseThrow();
        Categorie hauts     = categorieRepository.findByNom("Hauts").orElseThrow();
        Categorie chaussures= categorieRepository.findByNom("Chaussures").orElseThrow();
        Categorie robes     = categorieRepository.findByNom("Robes").orElseThrow();

        itemRepository.save(Item.builder()
                .titre("Manteau en laine vintage").description("Magnifique manteau beige 100% laine, coupe droite. Porté très peu.")
                .prix(new BigDecimal("29.00")).taille(Taille.M).etat(Etat.BON)
                .style(Style.VINTAGE).categorie(manteaux).vendeur(admin)
                .nbLikes(42).statut(StatutItem.DISPONIBLE).actif(true).build());

        itemRepository.save(Item.builder()
                .titre("Pull en cachemire crème").description("Pull doux et chaud, taille unique, lavage délicat uniquement.")
                .prix(new BigDecimal("22.00")).taille(Taille.UNIQUE).etat(Etat.COMME_NEUF)
                .style(Style.CASUAL).categorie(hauts).vendeur(admin)
                .nbLikes(28).statut(StatutItem.DISPONIBLE).actif(true).build());

        itemRepository.save(Item.builder()
                .titre("Sneakers Adidas Stan Smith").description("Blanches, taille 40, semelle propre. Authentiques.")
                .prix(new BigDecimal("45.00")).taille(Taille.T40).etat(Etat.BON)
                .style(Style.STREETWEAR).categorie(chaussures).vendeur(admin)
                .nbLikes(67).statut(StatutItem.DISPONIBLE).actif(true).build());

        itemRepository.save(Item.builder()
                .titre("Robe fleurie printemps").description("Robe légère à fleurs, parfaite pour l'été. Taille S.")
                .prix(new BigDecimal("18.00")).taille(Taille.S).etat(Etat.COMME_NEUF)
                .style(Style.BOHEME).categorie(robes).vendeur(admin)
                .nbLikes(53).statut(StatutItem.DISPONIBLE).actif(true).build());
    }
}