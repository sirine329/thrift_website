package com.thriftby.controller;

import com.thriftby.entity.*;
import com.thriftby.service.ImageStorageService;
import com.thriftby.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/vendeur")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class VendeurController {

    private final ItemService itemService;
    private final CategorieService categorieService;
    private final UserService userService;
    private final CommandeService commandeService;
    private final WishlistService wishlistService;
    private final ImageStorageService imageStorageService;

    // ====== DASHBOARD ======
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User currentUser, Model model) {
        List<Item> mesArticles = itemService.findByVendeur(currentUser.getId());
        model.addAttribute("user", currentUser);
        model.addAttribute("articles", mesArticles);
        model.addAttribute("mesItems", mesArticles);
        model.addAttribute("myArticles", mesArticles);
        model.addAttribute("commandes", commandeService.findByAcheteur(currentUser.getId()));
        model.addAttribute("wishlist", wishlistService.findByUser(currentUser.getId()));
        model.addAttribute("profileLevel", determineProfileLevel(mesArticles.size()));
        model.addAttribute("totalArticles", mesArticles.size());
        model.addAttribute("articleVendus", mesArticles.stream()
                .filter(Item::isVendu).count());
        model.addAttribute("articlesDisponibles", mesArticles.stream()
                .filter(Item::isDisponible).count());
        return "profil/dashboard";
    }

    // ====== PUBLIER UN ARTICLE ======
    @GetMapping("/publier")
    public String showPublierForm(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("item", new Item());
        model.addAttribute("categories", categorieService.findActives());
        model.addAttribute("tailles", Taille.values());
        model.addAttribute("etats", Etat.values());
        model.addAttribute("styles", Style.values());
        return "profil/publier-article";
    }

    @PostMapping(value = "/publier", consumes = "multipart/form-data")
    public String publierArticle(@AuthenticationPrincipal User currentUser,
                                  @Valid @ModelAttribute Item item,
                                  @RequestParam(required = false) List<Long> categorieIds,
                                  @RequestParam(required = false) MultipartFile[] photoFiles,
                                  @RequestParam(required = false) List<String> photos,
                                  BindingResult result,
                                  RedirectAttributes ra,
                                  Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categorieService.findActives());
            model.addAttribute("tailles", Taille.values());
            model.addAttribute("etats", Etat.values());
            model.addAttribute("styles", Style.values());
            return "profil/publier-article";
        }

        try {
            List<String> savedUrls = imageStorageService.saveAll(photoFiles);
            if (!savedUrls.isEmpty()) {
                if (item.getPhotoUrl() == null || item.getPhotoUrl().isBlank()) {
                    item.setPhotoUrl(savedUrls.get(0));
                }
                savedUrls.stream()
                        .map(url -> ItemPhoto.builder().photoUrl(url).owner(currentUser).build())
                        .forEach(item::addPhoto);
            }
            if (!hasUploadedFiles(photoFiles) && (item.getPhotoUrl() == null || item.getPhotoUrl().isBlank())) {
                result.rejectValue("photoUrl", "NotBlank", "Une photo principale est requise");
            }
            if (photos != null) {
                photos.stream()
                        .filter(url -> url != null && !url.isBlank())
                        .forEach(url -> item.addPhoto(ItemPhoto.builder().photoUrl(url).owner(currentUser).build()));
            }
            if (result.hasErrors()) {
                model.addAttribute("categories", categorieService.findActives());
                model.addAttribute("tailles", Taille.values());
                model.addAttribute("etats", Etat.values());
                model.addAttribute("styles", Style.values());
                return "profil/publier-article";
            }

            itemService.publier(item, currentUser, categorieIds);
            ra.addFlashAttribute("success", "Article publié avec succès !");
            return "redirect:/vendeur/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categorieService.findActives());
            model.addAttribute("tailles", Taille.values());
            model.addAttribute("etats", Etat.values());
            model.addAttribute("styles", Style.values());
            return "profil/publier-article";
        }
    }

    // ====== MODIFIER UN ARTICLE ======
    @GetMapping("/modifier/{id}")
    public String showModifierForm(@PathVariable Long id,
                                    @AuthenticationPrincipal User currentUser,
                                    Model model) {
        Item item = itemService.findById(id);
        
        // Vérifier que l'utilisateur est le vendeur
        if (!item.getVendeur().getId().equals(currentUser.getId())) {
            return "redirect:/vendeur/dashboard";
        }

        model.addAttribute("item", item);
        model.addAttribute("categories", categorieService.findActives());
        model.addAttribute("tailles", Taille.values());
        model.addAttribute("etats", Etat.values());
        model.addAttribute("styles", Style.values());
        return "profil/modifier-article";
    }

    @PostMapping(value = "/modifier/{id}", consumes = "multipart/form-data")
    public String modifierArticle(@PathVariable Long id,
                                   @AuthenticationPrincipal User currentUser,
                                   @Valid @ModelAttribute Item item,
                                   @RequestParam(required = false) List<Long> categorieIds,
                                   @RequestParam(required = false) MultipartFile[] photoFiles,
                                   @RequestParam(required = false) List<String> photos,
                                   BindingResult result,
                                   RedirectAttributes ra,
                                   Model model) {
        Item existingItem = itemService.findById(id);
        
        // Vérifier que l'utilisateur est le vendeur
        if (!existingItem.getVendeur().getId().equals(currentUser.getId())) {
            return "redirect:/vendeur/dashboard";
        }

        if (result.hasErrors()) {
            model.addAttribute("item", existingItem);
            model.addAttribute("categories", categorieService.findActives());
            model.addAttribute("tailles", Taille.values());
            model.addAttribute("etats", Etat.values());
            model.addAttribute("styles", Style.values());
            return "profil/modifier-article";
        }

        try {
            List<String> savedUrls = imageStorageService.saveAll(photoFiles);
            if (!savedUrls.isEmpty()) {
                if (item.getPhotoUrl() == null || item.getPhotoUrl().isBlank()) {
                    item.setPhotoUrl(savedUrls.get(0));
                }
                savedUrls.stream()
                        .map(url -> ItemPhoto.builder().photoUrl(url).owner(currentUser).build())
                        .forEach(item::addPhoto);
            }
            if (!hasUploadedFiles(photoFiles) && (item.getPhotoUrl() == null || item.getPhotoUrl().isBlank())) {
                result.rejectValue("photoUrl", "NotBlank", "Une photo principale est requise");
            }
            if (photos != null) {
                photos.stream()
                        .filter(url -> url != null && !url.isBlank())
                        .forEach(url -> item.addPhoto(ItemPhoto.builder().photoUrl(url).owner(currentUser).build()));
            }
            if (result.hasErrors()) {
                model.addAttribute("item", existingItem);
                model.addAttribute("categories", categorieService.findActives());
                model.addAttribute("tailles", Taille.values());
                model.addAttribute("etats", Etat.values());
                model.addAttribute("styles", Style.values());
                return "profil/modifier-article";
            }

            item.setId(id);
            itemService.update(item, categorieIds);
            ra.addFlashAttribute("success", "Article mis à jour !");
            return "redirect:/vendeur/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("item", existingItem);
            model.addAttribute("categories", categorieService.findActives());
            model.addAttribute("tailles", Taille.values());
            model.addAttribute("etats", Etat.values());
            model.addAttribute("styles", Style.values());
            return "profil/modifier-article";
        }
    }

    private boolean hasUploadedFiles(MultipartFile[] photoFiles) {
        return photoFiles != null && Arrays.stream(photoFiles)
                .anyMatch(file -> file != null && !file.isEmpty());
    }

    // ====== SUPPRIMER UN ARTICLE ======
    @GetMapping("/supprimer/{id}")
    public String supprimerArticle(@PathVariable Long id,
                                    @AuthenticationPrincipal User currentUser,
                                    RedirectAttributes ra) {
        Item item = itemService.findById(id);
        
        // Vérifier que l'utilisateur est le vendeur
        if (!item.getVendeur().getId().equals(currentUser.getId())) {
            return "redirect:/vendeur/dashboard";
        }

        try {
            itemService.delete(id);
            ra.addFlashAttribute("success", "Article supprimé !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendeur/dashboard";
    }

    @PostMapping("/supprimer/{id}")
    public String supprimerArticlePost(@PathVariable Long id,
                                       @AuthenticationPrincipal User currentUser,
                                       RedirectAttributes ra) {
        return supprimerArticle(id, currentUser, ra);
    }

    @PostMapping("/supprimer-photo/{itemId}/{photoId}")
    public String supprimerPhoto(@PathVariable Long itemId,
                                 @PathVariable Long photoId,
                                 @AuthenticationPrincipal User currentUser,
                                 RedirectAttributes ra) {
        Item item = itemService.findById(itemId);
        if (!item.getVendeur().getId().equals(currentUser.getId())) {
            return "redirect:/vendeur/dashboard";
        }
        try {
            itemService.deletePhoto(photoId, itemId, currentUser.getId());
            ra.addFlashAttribute("success", "Photo supprimée !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendeur/modifier/" + itemId;
    }

    // ====== MARQUER COMME VENDU ======
    @GetMapping("/marquer-vendu/{id}")
    public String marquerVendu(@PathVariable Long id,
                                @AuthenticationPrincipal User currentUser,
                                RedirectAttributes ra) {
        Item item = itemService.findById(id);
        
        // Vérifier que l'utilisateur est le vendeur
        if (!item.getVendeur().getId().equals(currentUser.getId())) {
            return "redirect:/vendeur/dashboard";
        }

        try {
            itemService.marquerVendu(id);
            ra.addFlashAttribute("success", "Article marqué comme vendu !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendeur/dashboard";
    }

    private String determineProfileLevel(long publishedCount) {
        if (publishedCount == 0) {
            return "Debutant";
        } else if (publishedCount < 5) {
            return "Intermediaire";
        } else if (publishedCount < 15) {
            return "Expert";
        }
        return "Maitre du vintage";
    }
}
