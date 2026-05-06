// src/main/java/com/asm/gestion_stagiaires/services/MessageService.java

package com.asm.gestion_stagiaires.services;

import com.asm.gestion_stagiaires.models.MessageResponse;

import java.util.List;

public interface MessageService {

    // Récupérer la conversation entre stagiaire et son encadrant
    List<MessageResponse> getConversation(Long stagiaireId);

    // Envoyer un message
    MessageResponse envoyerMessage(
            Long expediteurId,
            Long destinataireId,
            String contenu
    );
}