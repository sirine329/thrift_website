package com.thriftby.controller;

import com.thriftby.entity.Item;
import com.thriftby.entity.ModePaiement;
import com.thriftby.entity.Panier;
import com.thriftby.entity.User;
import com.thriftby.service.CommandeService;
import com.thriftby.service.PanierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommandeController {

    private final PanierService panierService;
    private final CommandeService commandeService;

    @GetMapping("/commande/checkout")
    public String checkout(@AuthenticationPrincipal User currentUser,
                           Model model,
                           RedirectAttributes ra) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        Panier panier = panierService.getPanier(currentUser.getId());
        if (panier == null || panier.getItems().isEmpty()) {
            ra.addFlashAttribute("info", "Votre panier est vide. Ajoutez un article avant de passer commande.");
            return "redirect:/panier";
        }

        model.addAttribute("panier", panier);
        model.addAttribute("modesPaiement", ModePaiement.values());
        return "boutique/checkout";
    }

    @PostMapping("/commande/checkout")
    public String passerCommande(@AuthenticationPrincipal User currentUser,
                                 @RequestParam String adresseLivraison,
                                 @RequestParam(defaultValue = "ESPECE_EN_LIVRAISON") ModePaiement modePaiement,
                                 RedirectAttributes ra) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (adresseLivraison == null || adresseLivraison.isBlank()) {
            ra.addFlashAttribute("error", "Veuillez saisir une adresse de livraison.");
            return "redirect:/commande/checkout";
        }

        Panier panier = panierService.getPanier(currentUser.getId());
        if (panier == null || panier.getItems().isEmpty()) {
            ra.addFlashAttribute("info", "Votre panier est vide. Ajoutez un article avant de passer commande.");
            return "redirect:/panier";
        }

        List<Item> items = new ArrayList<>(panier.getItems());
        for (Item item : items) {
            if (item.getVendeur() != null && item.getVendeur().getId().equals(currentUser.getId())) {
                ra.addFlashAttribute("error", "Vous ne pouvez pas acheter votre propre article : " + item.getTitre());
                return "redirect:/commande/checkout";
            }
            if (!item.isDisponible()) {
                ra.addFlashAttribute("error", "Cet article n'est plus disponible : " + item.getTitre());
                return "redirect:/commande/checkout";
            }
        }

        for (Item item : items) {
            try {
                commandeService.creerCommande(item, currentUser, adresseLivraison, modePaiement);
            } catch (RuntimeException e) {
                ra.addFlashAttribute("error", e.getMessage());
                return "redirect:/commande/checkout";
            }
        }

        panierService.vider(currentUser.getId());

        String successMessage = modePaiement == ModePaiement.EN_LIGNE
                ? "Commande enregistrée. Paiement en ligne choisi — consultez ensuite vos commandes depuis votre profil."
                : "Commande enregistrée. Paiement en espèces à la livraison sélectionné.";

        ra.addFlashAttribute("success", successMessage);
        return "redirect:/profil";
    }
}
