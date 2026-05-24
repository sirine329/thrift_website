package com.thriftby.repository;

import com.thriftby.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Basiques
    List<Item> findByStatutAndActifTrue(StatutItem statut);
    List<Item> findByVendeurId(Long vendeurId);
    List<Item> findByVendeurIdAndStatut(Long vendeurId, StatutItem statut);
    List<Item> findByCategorieId(Long categorieId);
    List<Item> findByCategorieIdAndStatutAndActifTrue(Long categorieId, StatutItem statut);

    @Query("SELECT i FROM Item i WHERE i.actif = true AND i.statut <> 'RETIRE' " +
            "AND i.categorie.id = :categorieId ORDER BY i.createdAt DESC")
    List<Item> findVisibleByCategorieId(@Param("categorieId") Long categorieId);

    // Recherche full-text
    @Query("SELECT i FROM Item i WHERE i.actif = true AND i.statut = 'DISPONIBLE' AND " +
            "(LOWER(i.titre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            " LOWER(i.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Item> search(@Param("q") String query);

    @Query("SELECT i FROM Item i WHERE i.actif = true AND i.statut <> 'RETIRE' AND " +
            "(LOWER(i.titre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            " LOWER(i.description) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "ORDER BY i.createdAt DESC")
    List<Item> searchVisible(@Param("q") String query);

    // Filtres combinés
    @Query("SELECT i FROM Item i WHERE i.actif = true AND i.statut = 'DISPONIBLE' " +
            "AND (:categorieId IS NULL OR i.categorie.id = :categorieId) " +
            "AND (:taille IS NULL OR i.taille = :taille) " +
            "AND (:etat IS NULL OR i.etat = :etat) " +
            "AND (:style IS NULL OR i.style = :style) " +
            "AND (:prixMin IS NULL OR i.prix >= :prixMin) " +
            "AND (:prixMax IS NULL OR i.prix <= :prixMax) " +
            "ORDER BY i.createdAt DESC")
    List<Item> findWithFilters(
            @Param("categorieId") Long categorieId,
            @Param("taille")      Taille taille,
            @Param("etat")        Etat etat,
            @Param("style")       Style style,
            @Param("prixMin")     BigDecimal prixMin,
            @Param("prixMax")     BigDecimal prixMax
    );

    @Query("SELECT i FROM Item i WHERE i.actif = true AND i.statut <> 'RETIRE' " +
            "AND (:categorieId IS NULL OR i.categorie.id = :categorieId) " +
            "AND (:taille IS NULL OR i.taille = :taille) " +
            "AND (:etat IS NULL OR i.etat = :etat) " +
            "AND (:style IS NULL OR i.style = :style) " +
            "AND (:prixMin IS NULL OR i.prix >= :prixMin) " +
            "AND (:prixMax IS NULL OR i.prix <= :prixMax) " +
            "ORDER BY i.createdAt DESC")
    List<Item> findVisibleWithFilters(
            @Param("categorieId") Long categorieId,
            @Param("taille")      Taille taille,
            @Param("etat")        Etat etat,
            @Param("style")       Style style,
            @Param("prixMin")     BigDecimal prixMin,
            @Param("prixMax")     BigDecimal prixMax
    );

    // Trending (les plus likés)
    @Query("SELECT i FROM Item i WHERE i.actif = true AND i.statut = 'DISPONIBLE' " +
            "ORDER BY i.nbLikes DESC")
    List<Item> findTrending();

    @Query("SELECT i FROM Item i WHERE i.actif = true AND i.statut <> 'RETIRE' " +
            "ORDER BY i.nbLikes DESC")
    List<Item> findVisibleTrending();

    // Nouveautés
    @Query("SELECT i FROM Item i WHERE i.actif = true AND i.statut = 'DISPONIBLE' " +
            "ORDER BY i.createdAt DESC")
    List<Item> findRecents();

    @Query("SELECT i FROM Item i WHERE i.actif = true AND i.statut <> 'RETIRE' " +
            "ORDER BY i.createdAt DESC")
    List<Item> findVisibleRecents();

    // Stats
    long countByStatut(StatutItem statut);
    long countByVendeurId(Long vendeurId);
    long countByActifTrue();

    @Query("SELECT SUM(i.prix) FROM Item i WHERE i.statut = 'VENDU'")
    BigDecimal sumPrixVendus();
}
