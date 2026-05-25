package com.thriftby.controller;

import com.thriftby.entity.*;
import com.thriftby.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
public class AdminController {

    private final ItemService itemService;
    private final CategorieService categorieService;
    private final UserService userService;
    private final CommandeService commandeService;

    // ====== DASHBOARD ======
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalItems",      itemService.countTotal());
        model.addAttribute("itemsDisponibles",itemService.countDisponibles());
        model.addAttribute("itemsVendus",     itemService.countVendus());
        model.addAttribute("totalUsers",      userService.countUsers());
        model.addAttribute("totalVendeurs",   userService.countUsers()); // à défaut, on met le total des utilisateurs
        model.addAttribute("totalCommandes",  commandeService.countTotal());
        model.addAttribute("commandesPayees", commandeService.countPayees());
        model.addAttribute("chiffreAffaires", commandeService.getChiffreAffaires());
        model.addAttribute("recentItems",     itemService.findRecents(5));
        model.addAttribute("recentCommandes", commandeService.findByStatut(StatutCommande.PAYEE)
                .stream().limit(5).toList());
        return "admin/dashboard";
    }

    // ... (le reste du contrôleur reste inchangé)
    // Assurez-vous de conserver toutes les autres méthodes telles quelles.
    // Je les réécris uniquement si nécessaire, mais elles sont correctes.
    // Pour des raisons d'espace, je ne copie pas les 100 lignes, mais elles restent identiques.
    // Il faut juste enlever la référence à countVendeurs().
    // Vous pouvez remplacer l'intégralité du fichier AdminController.java par celui-ci.

    // (Je mets ci-dessous le reste complet pour éviter toute confusion)

    // ====== ARTICLES (Products) ======
    @GetMapping("/articles")
    public String listArticles(@RequestParam(required = false) String search, Model model) {
        List<Item> items = (search != null && !search.isEmpty())
                ? itemService.search(search)
                : itemService.findAll();
        model.addAttribute("items", items);
        model.addAttribute("products", items);
        model.addAttribute("search", search);
        model.addAttribute("totalItems", itemService.countTotal());
        model.addAttribute("itemsDisponibles", itemService.countDisponibles());
        model.addAttribute("itemsVendus", itemService.countVendus());
        model.addAttribute("totalCategories", categorieService.countTotal());
        return "admin/products/list";
    }

    // ====== ROUTING ALIAS: /admin/products -> /admin/articles ======
    @GetMapping("/products")
    public String listProducts(@RequestParam(required = false) String search, Model model) {
        return listArticles(search, model);
    }

    @GetMapping("/articles/toggle/{id}")
    public String toggleArticle(@PathVariable Long id, RedirectAttributes ra) {
        itemService.toggleActif(id);
        ra.addFlashAttribute("info", "Statut de l'article modifié.");
        return "redirect:/admin/products";
    }

    @GetMapping("/articles/delete/{id}")
    public String deleteArticle(@PathVariable Long id, RedirectAttributes ra) {
        itemService.delete(id);
        ra.addFlashAttribute("success", "Article supprimé.");
        return "redirect:/admin/products";
    }

    // ====== ROUTING ALIASES FOR /admin/products/* ======
    @GetMapping("/products/toggle/{id}")
    public String toggleProduct(@PathVariable Long id, RedirectAttributes ra) {
        return toggleArticle(id, ra);
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        return deleteArticle(id, ra);
    }

    // ====== COMMANDES ======
    @GetMapping("/commandes")
    public String listCommandes(@RequestParam(required = false) String statut, Model model) {
        var commandes = (statut != null && !statut.isEmpty())
                ? commandeService.findByStatut(StatutCommande.valueOf(statut))
                : commandeService.findAll();
        model.addAttribute("commandes",       commandes);
        model.addAttribute("statuts",         StatutCommande.values());
        model.addAttribute("statutFiltre",    statut);
        return "admin/commandes/list";
    }

    @GetMapping("/commandes/{id}")
    public String detailCommande(@PathVariable Long id, Model model) {
        model.addAttribute("commande", commandeService.findById(id));
        return "admin/commandes/detail";
    }

    @GetMapping("/commandes/livrer/{id}")
    public String marquerLivree(@PathVariable Long id, RedirectAttributes ra) {
        commandeService.marquerLivree(id);
        ra.addFlashAttribute("success", "Commande marquée comme livrée.");
        return "redirect:/admin/commandes";
    }

    @GetMapping("/commandes/annuler/{id}")
    public String annulerCommande(@PathVariable Long id, RedirectAttributes ra) {
        try {
            commandeService.annuler(id);
            ra.addFlashAttribute("success", "Commande annulée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/commandes";
    }

    // ====== CATÉGORIES ======
    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories",   categorieService.findAll());
        model.addAttribute("newCategorie", new Categorie());
        return "admin/categories/list";
    }

    @PostMapping("/categories/save")
    public String saveCategorie(@Valid @ModelAttribute("newCategorie") Categorie categorie,
                                BindingResult result, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categorieService.findAll());
            return "admin/categories/list";
        }
        try {
            categorieService.save(categorie);
            ra.addFlashAttribute("success", "Catégorie ajoutée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategorie(@PathVariable Long id, Model model) {
        model.addAttribute("categorie", categorieService.findById(id));
        return "admin/categories/form";
    }

    @PostMapping("/categories/update")
    public String updateCategorie(@Valid @ModelAttribute("categorie") Categorie categorie,
                                  BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "admin/categories/form";
        categorieService.update(categorie);
        ra.addFlashAttribute("success", "Catégorie mise à jour.");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategorie(@PathVariable Long id, RedirectAttributes ra) {
        categorieService.delete(id);
        ra.addFlashAttribute("success", "Catégorie supprimée.");
        return "redirect:/admin/categories";
    }

    // ====== UTILISATEURS (Users) ======
    @GetMapping("/utilisateurs")
    public String listUsers(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String role,
                            Model model) {
        var users = (search != null && !search.isEmpty())
                ? userService.search(search)
                : (role != null && !role.isEmpty())
                  ? userService.findByRole(Role.valueOf(role))
                  : userService.findAll();
        model.addAttribute("users",      users);
        model.addAttribute("roles",      Role.values());
        model.addAttribute("search",     search);
        model.addAttribute("roleFitre",  role);
        return "admin/users/list";
    }

    // ====== ROUTING ALIAS: /admin/users -> /admin/utilisateurs ======
    @GetMapping("/users")
    public String listUsersAlias(@RequestParam(required = false) String search,
                                 @RequestParam(required = false) String role,
                                 Model model) {
        return listUsers(search, role, model);
    }

    @GetMapping("/utilisateurs/toggle/{id}")
    public String toggleUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.toggleActif(id);
        ra.addFlashAttribute("info", "Statut utilisateur modifié.");
        return "redirect:/admin/users";
    }

    @GetMapping("/utilisateurs/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.delete(id);
        ra.addFlashAttribute("success", "Utilisateur supprimé.");
        return "redirect:/admin/users";
    }

    // ====== ROUTING ALIASES FOR /admin/users/* ======
    @GetMapping("/users/toggle/{id}")
    public String toggleUserAlias(@PathVariable Long id, RedirectAttributes ra) {
        return toggleUser(id, ra);
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUserAlias(@PathVariable Long id, RedirectAttributes ra) {
        return deleteUser(id, ra);
    }
}
