package com.thriftby.service;

import com.thriftby.entity.*;
import com.thriftby.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PanierService {

    private final PanierRepository panierRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public Panier getPanier(Long userId) {
        return panierRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow();
                    Panier p = Panier.builder().user(user).build();
                    return panierRepository.save(p);
                });
    }

    public Panier ajouterItem(Long userId, Long itemId) {
        Panier panier = getPanier(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Article introuvable"));
        if (!item.isDisponible()) {
            throw new RuntimeException("Cet article n'est plus disponible");
        }
        if (!panier.getItems().contains(item)) {
            panier.getItems().add(item);
            panierRepository.save(panier);
        }
        return panier;
    }

    public Panier retirerItem(Long userId, Long itemId) {
        Panier panier = getPanier(userId);
        panier.getItems().removeIf(i -> i.getId().equals(itemId));
        return panierRepository.save(panier);
    }

    public void vider(Long userId) {
        Panier panier = getPanier(userId);
        panier.getItems().clear();
        panierRepository.save(panier);
    }
}
