package com.thriftby.service;

import com.thriftby.entity.*;
import com.thriftby.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategorieRepository categorieRepository;
    private final ItemPhotoRepository itemPhotoRepository;

    // ---- Lecture ----
    @Transactional(readOnly = true)
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Item> findDisponibles() {
        return itemRepository.findByStatutAndActifTrue(StatutItem.DISPONIBLE);
    }

    @Transactional(readOnly = true)
    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article introuvable: " + id));
    }

    @Transactional(readOnly = true)
    public List<Item> findByVendeur(Long vendeurId) {
        return itemRepository.findByVendeurId(vendeurId);
    }

    @Transactional(readOnly = true)
    public List<Item> findByCategorie(Long categorieId) {
        return itemRepository.findByCategorieIdAndStatutAndActifTrue(categorieId, StatutItem.DISPONIBLE);
    }

    @Transactional(readOnly = true)
    public List<Item> findVisibleByCategorie(Long categorieId) {
        return itemRepository.findVisibleByCategorieId(categorieId);
    }

    @Transactional(readOnly = true)
    public List<Item> search(String query) {
        return itemRepository.search(query);
    }

    @Transactional(readOnly = true)
    public List<Item> searchVisible(String query) {
        return itemRepository.searchVisible(query);
    }

    @Transactional(readOnly = true)
    public List<Item> findWithFilters(Long categorieId, Taille taille, Etat etat,
                                      Style style, BigDecimal prixMin, BigDecimal prixMax) {
        return itemRepository.findWithFilters(categorieId, taille, etat, style, prixMin, prixMax);
    }

    @Transactional(readOnly = true)
    public List<Item> findVisibleWithFilters(Long categorieId, Taille taille, Etat etat,
                                             Style style, BigDecimal prixMin, BigDecimal prixMax) {
        return itemRepository.findVisibleWithFilters(categorieId, taille, etat, style, prixMin, prixMax);
    }

    @Transactional(readOnly = true)
    public List<Item> findTrending(int limit) {
        return itemRepository.findTrending().stream().limit(limit).toList();
    }

    @Transactional(readOnly = true)
    public List<Item> findVisibleTrending(int limit) {
        return itemRepository.findVisibleTrending().stream().limit(limit).toList();
    }

    @Transactional(readOnly = true)
    public List<Item> findRecents(int limit) {
        return itemRepository.findRecents().stream().limit(limit).toList();
    }

    @Transactional(readOnly = true)
    public List<Item> findVisibleRecents(int limit) {
        return itemRepository.findVisibleRecents().stream().limit(limit).toList();
    }

    // ---- CRUD ----
    public Item publier(Item item, User vendeur, List<Long> categorieIds) {
        if (categorieIds == null || categorieIds.isEmpty()) {
            throw new RuntimeException("Vous devez sélectionner au moins une catégorie.");
        }
        List<Categorie> categories = categorieRepository.findAllById(categorieIds);
        if (categories.isEmpty()) {
            throw new RuntimeException("Catégorie introuvable");
        }
        item.setVendeur(vendeur);
        item.setCategories(categories);
        item.setCategorie(categories.get(0));
        item.setStatut(StatutItem.DISPONIBLE);
        item.setActif(true);
        if (item.getPhotos() != null) {
            item.getPhotos().stream()
                    .filter(java.util.Objects::nonNull)
                    .forEach(photo -> photo.setOwner(vendeur));
        }
        return itemRepository.save(item);
    }

    public Item update(Item item, List<Long> categorieIds) {
        Item existing = findById(item.getId());
        existing.setTitre(item.getTitre());
        existing.setDescription(item.getDescription());
        existing.setPrix(item.getPrix());
        existing.setTaille(item.getTaille());
        existing.setEtat(item.getEtat());
        existing.setStyle(item.getStyle());
        if (categorieIds != null && !categorieIds.isEmpty()) {
            List<Categorie> categories = categorieRepository.findAllById(categorieIds);
            if (!categories.isEmpty()) {
                existing.setCategories(categories);
                existing.setCategorie(categories.get(0));
            }
        }
        if (item.getPhotoUrl() != null && !item.getPhotoUrl().isBlank()) {
            existing.setPhotoUrl(item.getPhotoUrl());
        }
        if (item.getPhotos() != null && !item.getPhotos().isEmpty()) {
            item.getPhotos().stream()
                    .filter(java.util.Objects::nonNull)
                    .forEach(photo -> {
                        photo.setItem(existing);
                        if (photo.getOwner() == null) {
                            photo.setOwner(existing.getVendeur());
                        }
                        existing.getPhotos().add(photo);
                    });
        }
        return itemRepository.save(existing);
    }

    public void delete(Long id) {
        itemRepository.deleteById(id);
    }

    public void deletePhoto(Long photoId, Long itemId, Long userId) {
        Item item = findById(itemId);
        ItemPhoto photo = itemPhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo introuvable : " + photoId));
        if (!item.getId().equals(photo.getItem().getId())) {
            throw new RuntimeException("La photo n'appartient pas à cet article.");
        }
        if (!photo.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Action non autorisée.");
        }
        item.getPhotos().removeIf(p -> p.getId().equals(photoId));
        itemRepository.save(item);
        itemPhotoRepository.delete(photo);
    }

    public void toggleActif(Long id) {
        Item item = findById(id);
        item.setActif(!item.isActif());
        itemRepository.save(item);
    }

    public void marquerVendu(Long id) {
        Item item = findById(id);
        item.setStatut(StatutItem.VENDU);
        itemRepository.save(item);
    }

    public void toggleLike(Long id) {
        Item item = findById(id);
        item.setNbLikes(item.getNbLikes() + 1);
        itemRepository.save(item);
    }

    // ---- Stats ----
    public long countTotal()       { return itemRepository.count(); }
    public long countDisponibles() { return itemRepository.countByStatut(StatutItem.DISPONIBLE); }
    public long countVendus()      { return itemRepository.countByStatut(StatutItem.VENDU); }
    public long countByVendeurId(Long vendeurId) { return itemRepository.countByVendeurId(vendeurId); }

    public List<Categorie> getAllCategories() {
        return categorieRepository.findByActifTrue();
    }
}
