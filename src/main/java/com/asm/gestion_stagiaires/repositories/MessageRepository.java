// src/main/java/com/asm/gestion_stagiaires/repositories/MessageRepository.java

package com.asm.gestion_stagiaires.repositories;

import com.asm.gestion_stagiaires.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Récupère tous les messages entre deux utilisateurs
    @Query("""
        SELECT m FROM Message m
        WHERE (m.expediteur.id = :userId1 AND m.destinataire.id = :userId2)
           OR (m.expediteur.id = :userId2 AND m.destinataire.id = :userId1)
        ORDER BY m.dateEnvoi ASC
    """)
    List<Message> findConversation(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );
}