package com.thriftby.controller;

import com.thriftby.entity.Item;
import com.thriftby.entity.User;
import com.thriftby.service.*;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profil")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfilController {

    private final UserService userService;
    private final CommandeService commandeService;
    private final WishlistService wishlistService;
    private final ItemService itemService;

    @GetMapping
    public String profil(@AuthenticationPrincipal User currentUser, Model model) {
        List<Item> articles = itemService.findByVendeur(currentUser.getId());
        model.addAttribute("user",         currentUser);
        model.addAttribute("commandes",    commandeService.findByAcheteur(currentUser.getId()));
        model.addAttribute("wishlist",     wishlistService.findByUser(currentUser.getId()));
        model.addAttribute("mesItems",     articles);
        model.addAttribute("myArticles",   articles);
        model.addAttribute("profileLevel", determineProfileLevel(articles.size()));
        return "profil/dashboard";
    }

    @GetMapping("/commandes")
    public String mesCommandes(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("commandes", commandeService.findByAcheteur(currentUser.getId()));
        return "profil/commandes";
    }

    @GetMapping("/wishlist")
    public String maWishlist(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("wishlist", wishlistService.findByUser(currentUser.getId()));
        return "profil/wishlist";
    }

    @GetMapping("/edit")
    public String editProfil(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("user", currentUser);
        return "profil/edit";
    }

    @PostMapping("/edit")
    public String saveProfil(@AuthenticationPrincipal User currentUser,
                             @ModelAttribute User formUser,
                             RedirectAttributes ra) {
        userService.updateProfil(currentUser, formUser);
        ra.addFlashAttribute("success", "Profil mis à jour !");
        return "redirect:/profil";
    }

    private String determineProfileLevel(long publishedCount) {
        if (publishedCount == 0) {
            return "Débutant";
        } else if (publishedCount < 5) {
            return "Intermédiaire";
        } else if (publishedCount < 15) {
            return "Expert";
        }
        return "Maître du vintage";
    }
}
