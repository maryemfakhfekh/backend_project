package com.asm.gestion_stagiaires.Controller;

import com.asm.gestion_stagiaires.models.*;
import com.asm.gestion_stagiaires.repositories.DemandeAccesRepository;
import com.asm.gestion_stagiaires.repositories.UtilisateurRepository;
import com.asm.gestion_stagiaires.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private DemandeAccesRepository demandeAccesRepository;
    @Autowired private EmailService emailService;

    // ===== STATS DASHBOARD RH =====

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ROLE_RH') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getStats() {
        List<Utilisateur> tous = utilisateurRepository.findAll();

        long totalStagiaires = tous.stream()
                .filter(u -> u.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_STAGIAIRE")))
                .count();

        long totalEncadrants = tous.stream()
                .filter(u -> u.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ENCADRANT")))
                .count();

        long totalRH = tous.stream()
                .filter(u -> u.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_RH")))
                .count();

        return ResponseEntity.ok(Map.of(
                "totalStagiaires", totalStagiaires,
                "totalEncadrants", totalEncadrants,
                "totalRH",         totalRH
        ));
    }

    // ❌ SUPPRIMÉ : /creer-rh et /creer-encadrant
    // L'admin ne crée plus directement les comptes.
    // Les RH et Encadrants doivent passer par une demande d'accès.

    // ===== MODIFIER UN UTILISATEUR =====

    @PutMapping("/utilisateurs/{id}")
    public ResponseEntity<?> modifierUtilisateur(
            @PathVariable Long id,
            @RequestBody Map<String, String> data) {
        return utilisateurRepository.findById(id).map(user -> {
            if (data.containsKey("nom"))       user.setNom(data.get("nom"));
            if (data.containsKey("prenom"))    user.setPrenom(data.get("prenom"));
            if (data.containsKey("email"))     user.setEmail(data.get("email"));
            if (data.containsKey("telephone")) user.setTelephone(data.get("telephone"));

            if (user instanceof Encadrant encadrant) {
                if (data.containsKey("departement")) encadrant.setDepartement(data.get("departement"));
                if (data.containsKey("specialite"))  encadrant.setSpecialite(data.get("specialite"));
            }

            if (user instanceof Stagiaire stagiaire) {
                if (data.containsKey("etablissement"))
                    stagiaire.setEtablissement(data.get("etablissement"));
            }

            utilisateurRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Utilisateur modifié avec succès"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ===== ACTIVER / DÉSACTIVER =====

    @PutMapping("/utilisateurs/{id}/activer")
    public ResponseEntity<?> activerCompte(@PathVariable Long id) {
        return utilisateurRepository.findById(id).map(user -> {
            user.setActif(true);
            utilisateurRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Compte activé"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/utilisateurs/{id}/desactiver")
    public ResponseEntity<?> desactiverCompte(@PathVariable Long id) {
        return utilisateurRepository.findById(id).map(user -> {
            user.setActif(false);
            utilisateurRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Compte désactivé"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ===== DEMANDES D'ACCÈS =====

    @GetMapping("/demandes-acces")
    public ResponseEntity<List<DemandeAcces>> getDemandesEnAttente() {
        return ResponseEntity.ok(demandeAccesRepository.findByStatut(StatutDemande.EN_ATTENTE));
    }

    @GetMapping("/demandes-acces/toutes")
    public ResponseEntity<List<DemandeAcces>> getToutesDemandes() {
        return ResponseEntity.ok(demandeAccesRepository.findAll());
    }

    // ✅ Validation : utilise le mot de passe DÉJÀ choisi et encodé par l'utilisateur
    @PutMapping("/demandes-acces/{id}/valider")
    public ResponseEntity<?> validerDemande(@PathVariable Long id) {
        DemandeAcces demande = demandeAccesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Cette demande a déjà été traitée"));
        }

        if (utilisateurRepository.existsByEmail(demande.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Un compte avec cet email existe déjà"));
        }

        // ✅ Création du compte avec le password déjà encodé dans la demande
        if ("RH".equalsIgnoreCase(demande.getRoleSouhaite())) {
            RH rh = new RH();
            rh.setNom(demande.getNom());
            rh.setPrenom(demande.getPrenom());
            rh.setEmail(demande.getEmail());
            rh.setPassword(demande.getPassword()); // déjà encodé
            rh.setTelephone(demande.getTelephone());
            rh.setActif(true);
            utilisateurRepository.save(rh);
        } else if ("ENCADRANT".equalsIgnoreCase(demande.getRoleSouhaite())) {
            Encadrant encadrant = new Encadrant();
            encadrant.setNom(demande.getNom());
            encadrant.setPrenom(demande.getPrenom());
            encadrant.setEmail(demande.getEmail());
            encadrant.setPassword(demande.getPassword()); // déjà encodé
            encadrant.setTelephone(demande.getTelephone());
            encadrant.setDepartement(demande.getDepartement());
            encadrant.setSpecialite(demande.getSpecialite());
            encadrant.setActif(true);
            utilisateurRepository.save(encadrant);
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Rôle inconnu : " + demande.getRoleSouhaite()));
        }

        // ✅ Email d'activation SANS identifiants (l'utilisateur connaît son mdp)
        emailService.envoyerEmailActivation(
                demande.getEmail(),
                demande.getNom(),
                demande.getPrenom(),
                demande.getRoleSouhaite()
        );

        demande.setStatut(StatutDemande.VALIDEE);
        demandeAccesRepository.save(demande);

        return ResponseEntity.ok(Map.of(
                "message", "Demande validée, compte activé et email envoyé"
        ));
    }

    // ✅ Refus : envoyer un email pour informer l'utilisateur
    @PutMapping("/demandes-acces/{id}/refuser")
    public ResponseEntity<?> refuserDemande(@PathVariable Long id) {
        DemandeAcces demande = demandeAccesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Cette demande a déjà été traitée"));
        }

        demande.setStatut(StatutDemande.REFUSEE);
        demandeAccesRepository.save(demande);

        // ✅ Email de refus
        emailService.envoyerEmailRefus(
                demande.getEmail(),
                demande.getNom(),
                demande.getPrenom(),
                demande.getRoleSouhaite()
        );

        return ResponseEntity.ok(Map.of("message", "Demande refusée et email envoyé"));
    }

    // ===== GESTION UTILISATEURS =====

    @GetMapping("/utilisateurs")
    public ResponseEntity<List<Utilisateur>> getAllUtilisateurs() {
        return ResponseEntity.ok(utilisateurRepository.findAll());
    }

    @DeleteMapping("/utilisateurs/{id}")
    public ResponseEntity<?> supprimerUtilisateur(@PathVariable Long id) {
        utilisateurRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé"));
    }

}