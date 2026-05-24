package com.thriftby.controller;

import com.thriftby.entity.*;
import com.thriftby.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class BoutiqueController {

    private final ItemService itemService;
    private final CategorieService categorieService;
    private final PanierService panierService;
    private final WishlistService wishlistService;
    private final CommentaireService commentaireService;
    private final UserService userService;

    // ====== RECOUVREMENT DE LA RACINE ======
    @GetMapping("/")
    public String redirigerRacine() {
        return "redirect:/boutique";
    }

    // ====== PAGE D'ACCUEIL ======
    @GetMapping("/boutique")
    public String accueil(Model model) {
        model.addAttribute("trending",   itemService.findVisibleTrending(8));
        model.addAttribute("recents",    itemService.findVisibleRecents(8));
        model.addAttribute("categories", categorieService.findActives());
        return "boutique/accueil";
    }

    // ====== CATALOGUE (SHOP) ======
    @GetMapping("/boutique/shop")
    public String shop(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Long categorieId,
                       @RequestParam(required = false) String taille,
                       @RequestParam(required = false) String etat,
                       @RequestParam(required = false) String style,
                       @RequestParam(required = false) BigDecimal prixMin,
                       @RequestParam(required = false) BigDecimal prixMax,
                       @AuthenticationPrincipal User currentUser,
                       Model model) {

        // Parsing des enums
        Taille tailleEnum = parseSafe(taille, Taille.class);
        Etat etatEnum     = parseSafe(etat, Etat.class);
        Style styleEnum   = parseSafe(style, Style.class);

        var items = (q != null && !q.isBlank())
                ? itemService.searchVisible(q)
                : itemService.findVisibleWithFilters(categorieId, tailleEnum, etatEnum, styleEnum, prixMin, prixMax);

        model.addAttribute("items",       items);
        model.addAttribute("categories",  categorieService.findActives());
        model.addAttribute("tailles",     Taille.values());
        model.addAttribute("etats",       Etat.values());
        model.addAttribute("styles",      Style.values());
        model.addAttribute("nbResultats", items.size());
        // Repasser les filtres actifs
        model.addAttribute("q",           q);
        model.addAttribute("categorieId", categorieId);
        model.addAttribute("taille",      taille);
        model.addAttribute("etat",        etat);
        model.addAttribute("style",       style);
        model.addAttribute("prixMin",     prixMin);
        model.addAttribute("prixMax",     prixMax);
        model.addAttribute("currentUser", currentUser);
        return "boutique/shop";
    }

    // ====== DÉTAIL ARTICLE ======
    @GetMapping("/boutique/article/{id}")
    public String articleDetail(@PathVariable Long id,
                                @AuthenticationPrincipal User currentUser,
                                Model model) {
        Item item = itemService.findById(id);
        model.addAttribute("item", item);
        model.addAttribute("autresArticles",
                itemService.findVisibleByCategorie(item.getCategorie().getId())
                        .stream()
                        .filter(i -> !i.getId().equals(id))
                        .limit(4).toList());
        if (currentUser != null) {
            model.addAttribute("dansWishlist",
                    wishlistService.isInWishlist(currentUser.getId(), id));
            model.addAttribute("isOwner", item.getVendeur().getId().equals(currentUser.getId()));
        } else {
            model.addAttribute("isOwner", false);
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("comments", commentaireService.findByItem(id));
        return "user/product-detail";
    }

    @PostMapping("/boutique/article/{id}/commenter")
    public String commenterArticle(@PathVariable Long id,
                                   @AuthenticationPrincipal User currentUser,
                                   @RequestParam String contenu,
                                   RedirectAttributes ra) {
        if (currentUser == null) {
            return "redirect:/login";
        }
        try {
            commentaireService.saveCommentaire(currentUser, id, contenu);
            ra.addFlashAttribute("success", "Commentaire ajouté !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/boutique/article/" + id;
    }

    // ====== PANIER ======
    @GetMapping("/panier")
    public String voirPanier(@AuthenticationPrincipal User currentUser, Model model) {
        if (currentUser == null) return "redirect:/login";
        Panier panier = panierService.getPanier(currentUser.getId());
        model.addAttribute("panier", panier);
        return "boutique/panier";
    }

    @PostMapping("/panier/ajouter/{itemId}")
    public String ajouterAuPanier(@PathVariable Long itemId,
                                  @AuthenticationPrincipal User currentUser,
                                  @RequestParam(defaultValue = "false") boolean orderNow,
                                  RedirectAttributes ra) {
        if (currentUser == null) return "redirect:/login";
        try {
            panierService.ajouterItem(currentUser.getId(), itemId);
            ra.addFlashAttribute("success", "Article ajouté au panier !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return orderNow ? "redirect:/commande/checkout" : "redirect:/boutique/article/" + itemId;
    }

    @GetMapping("/panier/retirer/{itemId}")
    public String retirerDuPanier(@PathVariable Long itemId,
                                  @AuthenticationPrincipal User currentUser,
                                  RedirectAttributes ra) {
        if (currentUser == null) return "redirect:/login";
        panierService.retirerItem(currentUser.getId(), itemId);
        ra.addFlashAttribute("info", "Article retiré du panier.");
        return "redirect:/panier";
    }

    // ====== WISHLIST ======
    @PostMapping("/wishlist/toggle/{itemId}")
    public String toggleWishlist(@PathVariable Long itemId,
                                 @AuthenticationPrincipal User currentUser,
                                 @RequestParam(defaultValue = "/boutique") String returnTo,
                                 RedirectAttributes ra) {
        if (currentUser == null) return "redirect:/login";
        wishlistService.toggle(currentUser.getId(), itemId);
        return "redirect:" + returnTo;
    }

    // ====== PROFIL PUBLIC VENDEUR ======
    @GetMapping("/vendeur/{id}")
    public String profilVendeur(@PathVariable Long id, Model model) {
        model.addAttribute("vendeur", userService.findById(id));
        model.addAttribute("vendeurItems", itemService.findByVendeur(id)
                .stream().filter(item -> item.isActif() && item.getStatut() != StatutItem.RETIRE).toList());
        return "boutique/profil-vendeur";
    }

    // Util
    private <T extends Enum<T>> T parseSafe(String val, Class<T> type) {
        if (val == null || val.isBlank()) return null;
        try { return Enum.valueOf(type, val); }
        catch (IllegalArgumentException e) { return null; }
    }
}
