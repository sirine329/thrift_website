package com.thriftby.repository;

import com.thriftby.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Tous les messages d'une conversation (dans les deux sens)
    @Query("SELECT m FROM Message m WHERE " +
            "(m.expediteur.id = :userId1 AND m.destinataire.id = :userId2) OR " +
            "(m.expediteur.id = :userId2 AND m.destinataire.id = :userId1) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findConversation(Long userId1, Long userId2);

    // Messages non lus pour un user
    List<Message> findByDestinataireIdAndLuFalse(Long destinataireId);

    long countByDestinataireIdAndLuFalse(Long destinataireId);
}
