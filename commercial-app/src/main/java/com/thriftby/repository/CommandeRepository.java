package com.thriftby.repository;

import com.thriftby.entity.Commande;
import com.thriftby.entity.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findByAcheteurIdOrderByCreatedAtDesc(Long acheteurId);
    List<Commande> findByVendeurIdOrderByCreatedAtDesc(Long vendeurId);
    List<Commande> findByStatutOrderByCreatedAtDesc(StatutCommande statut);
    Optional<Commande> findByNumero(String numero);
    Optional<Commande> findByStripeSessionId(String sessionId);
    long countByStatut(StatutCommande statut);

    @Query("SELECT SUM(c.montantTotal) FROM Commande c WHERE c.statut IN ('PAYEE','EXPEDIEE','LIVREE')")
    BigDecimal sumChiffreAffaires();
}