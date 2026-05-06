package com.asm.gestion_stagiaires.Controller;

import com.asm.gestion_stagiaires.models.DemandeAcces;
import com.asm.gestion_stagiaires.models.StatutDemande;
import com.asm.gestion_stagiaires.repositories.DemandeAccesRepository;
import com.asm.gestion_stagiaires.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demandes-acces")
@CrossOrigin("*")
public class DemandeAccesController {

    @Autowired private DemandeAccesRepository demandeAccesRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    /**
     * Endpoint commun pour RH (web) et Encadrant (mobile).
     * Le frontend envoie roleSouhaite = "RH" ou "ENCADRANT".
     * Les champs departement/specialite sont obligatoires UNIQUEMENT pour Encadrant.
     */
    @PostMapping
    public ResponseEntity<?> soumettreDemande(@RequestBody DemandeAcces demande) {

        // ===== Validations communes =====

        if (demande.getNom() == null || demande.getNom().isBlank()
                || demande.getPrenom() == null || demande.getPrenom().isBlank()
                || demande.getEmail() == null || demande.getEmail().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Nom, prénom et email sont obligatoires"));
        }

        if (demande.getPassword() == null || demande.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Le mot de passe est obligatoire"));
        }

        if (demande.getPassword().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Le mot de passe doit contenir au moins 6 caractères"));
        }

        if (demande.getRoleSouhaite() == null || demande.getRoleSouhaite().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Le rôle souhaité est obligatoire (RH ou ENCADRANT)"));
        }

        String role = demande.getRoleSouhaite().toUpperCase();
        if (!role.equals("RH") && !role.equals("ENCADRANT")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Le rôle doit être RH ou ENCADRANT"));
        }

        // ===== Vérifier les doublons =====

        if (utilisateurRepository.existsByEmail(demande.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Un compte avec cet email existe déjà"));
        }

        if (demandeAccesRepository.existsByEmail(demande.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Une demande avec cet email a déjà été soumise"));
        }

        // ===== Validations spécifiques par rôle =====

        if (role.equals("ENCADRANT")) {
            // ✅ Encadrant (mobile) → departement et specialite obligatoires
            if (demande.getDepartement() == null || demande.getDepartement().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Le département est obligatoire pour un encadrant"));
            }
            if (demande.getSpecialite() == null || demande.getSpecialite().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "La spécialité est obligatoire pour un encadrant"));
            }
        } else {
            // ✅ RH (web) → on ignore departement/specialite même si envoyés par erreur
            demande.setDepartement(null);
            demande.setSpecialite(null);
        }

        // ===== Sauvegarde =====

        demande.setRoleSouhaite(role);
        demande.setPassword(passwordEncoder.encode(demande.getPassword()));
        demande.setStatut(StatutDemande.EN_ATTENTE);

        demandeAccesRepository.save(demande);

        return ResponseEntity.ok(Map.of(
                "message", "Demande d'accès soumise avec succès. Vous recevrez un email après validation par l'administrateur."
        ));
    }
}