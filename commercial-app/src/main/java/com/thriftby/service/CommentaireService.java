package com.thriftby.service;

import com.thriftby.entity.Commentaire;
import com.thriftby.entity.Item;
import com.thriftby.entity.User;
import com.thriftby.repository.CommentaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentaireService {

    private final CommentaireRepository commentaireRepository;
    private final ItemService itemService;

    @Transactional(readOnly = true)
    public List<Commentaire> findByItem(Long itemId) {
        return commentaireRepository.findByItemIdOrderByCreatedAtDesc(itemId);
    }

    public Commentaire saveCommentaire(User auteur, Long itemId, String contenu) {
        if (contenu == null || contenu.isBlank()) {
            throw new RuntimeException("Le commentaire ne peut pas être vide.");
        }
        Item item = itemService.findById(itemId);
        Commentaire commentaire = Commentaire.builder()
                .auteur(auteur)
                .item(item)
                .contenu(contenu.trim())
                .build();
        return commentaireRepository.save(commentaire);
    }
}
