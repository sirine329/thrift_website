package com.thriftby.service;

import com.thriftby.entity.*;
import com.thriftby.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ItemRepository itemRepository;

    // ---- Lecture ----
    @Transactional(readOnly = true)
    public List<Commande> findAll() { return commandeRepository.findAll(); }

    @Transactional(readOnly = true)
    public Commande findById(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable: " + id));
    }

    @Transactional(readOnly = true)
    public List<Commande> findByAcheteur(Long acheteurId) {
        return commandeRepository.findByAcheteurIdOrderByCreatedAtDesc(acheteurId);
    }

    @Transactional(readOnly = true)
    public List<Commande> findByVendeur(Long vendeurId) {
        return commandeRepository.findByVendeurIdOrderByCreatedAtDesc(vendeurId);
    }

    @Transactional(readOnly = true)
    public List<Commande> findByStatut(StatutCommande statut) {
        return commandeRepository.findByStatutOrderByCreatedAtDesc(statut);
    }

    // ---- Création ----
    public Commande creerCommande(Item item, User acheteur, String adresseLivraison) {
        return creerCommande(item, acheteur, adresseLivraison, ModePaiement.ESPECE_EN_LIVRAISON);
    }

    public Commande creerCommande(Item item, User acheteur, String adresseLivraison, ModePaiement modePaiement) {
        if (!item.isDisponible()) {
            throw new RuntimeException("Cet article n'est plus disponible.");
        }
        if (item.getVendeur().getId().equals(acheteur.getId())) {
            throw new RuntimeException("Vous ne pouvez pas acheter votre propre article.");
        }

        BigDecimal montantItem      = item.getPrix();
        BigDecimal fraisPlateforme  = montantItem.multiply(new BigDecimal("0.05"));
        BigDecimal fraisLivraison   = new BigDecimal("7.00");
        BigDecimal total            = montantItem.add(fraisPlateforme).add(fraisLivraison);

        Commande commande = Commande.builder()
                .numero(genererNumero())
                .item(item)
                .acheteur(acheteur)
                .vendeur(item.getVendeur())
                .montantItem(montantItem)
                .fraisPlateforme(fraisPlateforme)
                .fraisLivraison(fraisLivraison)
                .montantTotal(total)
                .adresseLivraison(adresseLivraison)
                .modePaiement(modePaiement)
                .statut(StatutCommande.EN_ATTENTE)
                .build();

        // Réserver l'article
        item.setStatut(StatutItem.RESERVE);
        itemRepository.save(item);

        return commandeRepository.save(commande);
    }

    // ---- Webhook Stripe ----
    public void confirmerPaiement(String stripeSessionId, String stripePaymentId) {
        Commande commande = commandeRepository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable pour session: " + stripeSessionId));

        commande.setStatut(StatutCommande.PAYEE);
        commande.setStripePaymentId(stripePaymentId);
        commande.setPaidAt(LocalDateTime.now());

        // Marquer l'article comme vendu (plus d'incrément vendeur)
        Item item = commande.getItem();
        item.setStatut(StatutItem.VENDU);
        itemRepository.save(item);

        commandeRepository.save(commande);
        log.info("✅ Commande payée: {}", commande.getNumero());
    }

    public void marquerExpediee(Long id, String numeroSuivi) {
        Commande commande = findById(id);
        commande.setStatut(StatutCommande.EXPEDIEE);
        commande.setNumeroSuivi(numeroSuivi);
        commande.setShippedAt(LocalDateTime.now());
        commandeRepository.save(commande);
    }

    public void marquerLivree(Long id) {
        Commande commande = findById(id);
        commande.setStatut(StatutCommande.LIVREE);
        commande.setDeliveredAt(LocalDateTime.now());
        commandeRepository.save(commande);
    }

    public void annuler(Long id) {
        Commande commande = findById(id);
        if (commande.isPayee()) {
            throw new RuntimeException("Impossible d'annuler une commande déjà payée.");
        }
        commande.setStatut(StatutCommande.ANNULEE);
        // Remettre l'article disponible
        Item item = commande.getItem();
        item.setStatut(StatutItem.DISPONIBLE);
        itemRepository.save(item);
        commandeRepository.save(commande);
    }

    // ---- Stats ----
    public long countTotal()     { return commandeRepository.count(); }
    public long countPayees()    { return commandeRepository.countByStatut(StatutCommande.PAYEE); }
    public long countExpediees() { return commandeRepository.countByStatut(StatutCommande.EXPEDIEE); }

    public BigDecimal getChiffreAffaires() {
        BigDecimal ca = commandeRepository.sumChiffreAffaires();
        return ca != null ? ca : BigDecimal.ZERO;
    }

    // ---- Helpers ----
    private String genererNumero() {
        long count = commandeRepository.count() + 1;
        return String.format("TB-%d-%05d", LocalDateTime.now().getYear(), count);
    }
}