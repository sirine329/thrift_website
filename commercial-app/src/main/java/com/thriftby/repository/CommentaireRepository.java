package com.thriftby.repository;

import com.thriftby.entity.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {
    List<Commentaire> findByItemIdOrderByCreatedAtDesc(Long itemId);
}
