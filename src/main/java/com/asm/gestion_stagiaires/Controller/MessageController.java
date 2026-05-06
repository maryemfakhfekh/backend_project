// src/main/java/com/asm/gestion_stagiaires/Controller/MessageController.java

package com.asm.gestion_stagiaires.Controller;

import com.asm.gestion_stagiaires.models.MessageRequest;
import com.asm.gestion_stagiaires.models.MessageResponse;
import com.asm.gestion_stagiaires.models.Utilisateur;
import com.asm.gestion_stagiaires.services.CandidatureService;
import com.asm.gestion_stagiaires.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin("*")
public class MessageController {

    @Autowired private MessageService     messageService;
    @Autowired private CandidatureService candidatureService;

    // ── GET /api/messages/{stagiaireId} ──────────────────
    // Récupère la conversation entre le stagiaire et son encadrant
    // Accessible par STAGIAIRE et ENCADRANT
    @GetMapping("/{stagiaireId}")
    @PreAuthorize("hasAuthority('ROLE_STAGIAIRE') or hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @PathVariable Long stagiaireId) {
        return ResponseEntity.ok(
                messageService.getConversation(stagiaireId)
        );
    }

    // ── POST /api/messages ───────────────────────────────
    // Envoyer un message (stagiaire ou encadrant)
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_STAGIAIRE') or hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<MessageResponse> envoyer(
            @RequestBody MessageRequest body,
            Principal principal) {

        return ResponseEntity.ok(
                messageService.envoyerMessage(
                        body.getExpediteurId(),
                        body.getDestinataireId(),
                        body.getContenu()
                )
        );
    }
}