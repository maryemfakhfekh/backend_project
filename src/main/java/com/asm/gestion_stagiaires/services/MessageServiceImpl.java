// src/main/java/com/asm/gestion_stagiaires/services/MessageServiceImpl.java

package com.asm.gestion_stagiaires.services;

import com.asm.gestion_stagiaires.models.Message;
import com.asm.gestion_stagiaires.models.MessageResponse;
import com.asm.gestion_stagiaires.models.Stage;
import com.asm.gestion_stagiaires.models.Utilisateur;
import com.asm.gestion_stagiaires.repositories.MessageRepository;
import com.asm.gestion_stagiaires.repositories.StageRepository;
import com.asm.gestion_stagiaires.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired private MessageRepository     messageRepository;
    @Autowired private StageRepository stagiaireRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;

    @Override
    public List<MessageResponse> getConversation(Long stagiaireId) {

        // Récupérer le stagiaire
        Stage stagiaire = stagiaireRepository.findById(stagiaireId)
                .orElseThrow(() -> new RuntimeException("Stagiaire non trouvé : " + stagiaireId));

        // Vérifier qu'un encadrant est affecté
        if (stagiaire.getEncadrant() == null) {
            return List.of();
        }

        // encadrant est directement un Utilisateur dans Stagiaire
        Long stagiaireUserId = stagiaire.getUtilisateur().getId();
        Long encadrantUserId = stagiaire.getEncadrant().getId();

        return messageRepository
                .findConversation(stagiaireUserId, encadrantUserId)
                .stream()
                .map(MessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public MessageResponse envoyerMessage(
            Long expediteurId,
            Long destinataireId,
            String contenu) {

        Utilisateur expediteur = utilisateurRepository
                .findById(expediteurId)
                .orElseThrow(() -> new RuntimeException("Expéditeur non trouvé : " + expediteurId));

        Utilisateur destinataire = utilisateurRepository
                .findById(destinataireId)
                .orElseThrow(() -> new RuntimeException("Destinataire non trouvé : " + destinataireId));

        Message message = new Message();
        message.setExpediteur(expediteur);
        message.setDestinataire(destinataire);
        message.setContenu(contenu);

        return MessageResponse.fromEntity(messageRepository.save(message));
    }
}