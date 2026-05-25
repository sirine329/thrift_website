package com.thriftby.controller;

import com.thriftby.entity.Role;
import com.thriftby.entity.User;
import com.thriftby.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;   // <-- AJOUTE CETTE LIGNE

@Controller
@RequestMapping("/superadmin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERADMIN')")
public class SuperAdminController {

    private final UserService userService;
    private final ItemService itemService;
    private final CategorieService categorieService;
    private final CommandeService commandeService;

    // ====== DASHBOARD ======
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers",      userService.countUsers());
        model.addAttribute("totalVendeurs",   userService.countUsers());
        model.addAttribute("totalAdmins",     userService.countAdmins());
        model.addAttribute("totalActifs",     userService.countActifs());
        model.addAttribute("totalTotal",      userService.countTotal());
        model.addAttribute("totalItems",      itemService.countTotal());
        model.addAttribute("totalVendus",     itemService.countVendus());
        model.addAttribute("totalCategories", categorieService.countTotal());
        model.addAttribute("totalCommandes",  commandeService.countTotal());
        model.addAttribute("chiffreAffaires", commandeService.getChiffreAffaires());
        model.addAttribute("allUsers",        userService.findAll());
        model.addAttribute("topVendeurs",     userService.findByRole(Role.USER).stream()
                .map(user -> new TopVendeur(user.getPrenom() + " " + user.getNom(),
                        user.getEmail(),
                        itemService.countByVendeurId(user.getId())))
                .filter(vendeur -> vendeur.nbVentes() > 0)
                .sorted((a, b) -> Long.compare(b.nbVentes(), a.nbVentes()))
                .limit(5)
                .toList());
        return "superadmin/dashboard";
    }

    private record TopVendeur(String nom, String email, long nbVentes) {}

    // ====== GESTION ADMINS ======
    @GetMapping("/admins")
    public String listAdmins(Model model) {
        model.addAttribute("admins",   userService.findByRole(Role.ADMIN));
        model.addAttribute("newAdmin", new User());
        return "superadmin/admins/list";
    }

    @PostMapping("/admins/save")
    public String saveAdmin(@Valid @ModelAttribute("newAdmin") User admin,
                            BindingResult result,
                            RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("admins", userService.findByRole(Role.ADMIN));
            return "superadmin/admins/list";
        }
        admin.setRole(Role.ADMIN);
        try {
            userService.save(admin);
            ra.addFlashAttribute("success", "Admin créé avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/superadmin/admins";
    }

    @GetMapping("/admins/edit/{id}")
    public String editAdmin(@PathVariable Long id, Model model) {
        model.addAttribute("admin", userService.findById(id));
        return "superadmin/admins/form";
    }

    @PostMapping("/admins/update")
    public String updateAdmin(@Valid @ModelAttribute("admin") User admin,
                              BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "superadmin/admins/form";
        admin.setRole(Role.ADMIN);
        userService.update(admin);
        ra.addFlashAttribute("success", "Admin mis à jour.");
        return "redirect:/superadmin/admins";
    }

    @GetMapping("/admins/delete/{id}")
    public String deleteAdmin(@PathVariable Long id, RedirectAttributes ra) {
        userService.delete(id);
        ra.addFlashAttribute("success", "Admin supprimé.");
        return "redirect:/superadmin/admins";
    }

    @GetMapping("/admins/toggle/{id}")
    public String toggleAdmin(@PathVariable Long id, RedirectAttributes ra) {
        userService.toggleActif(id);
        ra.addFlashAttribute("info", "Statut admin modifié.");
        return "redirect:/superadmin/admins";
    }

    // ====== TOUS LES UTILISATEURS ======
    @GetMapping("/users")
    public String allUsers(@RequestParam(required = false) String search, Model model) {
        var users = (search != null && !search.isEmpty())
                ? userService.search(search)
                : userService.findAll();
        model.addAttribute("users",  users);
        model.addAttribute("search", search);
        return "superadmin/users/list";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.delete(id);
        ra.addFlashAttribute("success", "Utilisateur supprimé.");
        return "redirect:/superadmin/users";
    }

    @GetMapping("/users/toggle/{id}")
    public String toggleUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.toggleActif(id);
        ra.addFlashAttribute("info", "Statut modifié.");
        return "redirect:/superadmin/users";
    }
}
