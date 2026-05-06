package com.asm.gestion_stagiaires.Controller;

import com.asm.gestion_stagiaires.models.StatusTache;
import com.asm.gestion_stagiaires.models.Tache;
import com.asm.gestion_stagiaires.services.CandidatureService;
import com.asm.gestion_stagiaires.services.StageService;
import com.asm.gestion_stagiaires.services.TacheService;
import com.asm.gestion_stagiaires.models.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/taches")
@CrossOrigin("*")
public class TacheController {

    @Autowired private TacheService tacheService;
    @Autowired private CandidatureService candidatureService;
    @Autowired private StageService stagiaireService;

    // Encadrant crée une tâche pour un stagiaire
    @PostMapping("/stagiaire/{stagiaireId}")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<Tache> creer(
            @PathVariable Long stagiaireId,
            @RequestBody Tache tache,
            Principal principal) {
        Utilisateur encadrant = candidatureService.getUtilisateurByEmail(principal.getName());
        return ResponseEntity.ok(
                tacheService.creerTache(stagiaireId, encadrant.getId(), tache));
    }

    // Stagiaire voit ses tâches
    @GetMapping("/mes-taches")
    @PreAuthorize("hasAuthority('ROLE_STAGIAIRE')")
    public ResponseEntity<List<Tache>> mesTaches(Principal principal) {
        Utilisateur utilisateur = candidatureService.getUtilisateurByEmail(principal.getName());
        var stagiaire = stagiaireService.getStageByUtilisateurId(utilisateur.getId());
        return ResponseEntity.ok(tacheService.getTachesByStagiaire(stagiaire.getId()));
    }

    // Encadrant voit les tâches de ses stagiaires
    @GetMapping("/encadrant")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<List<Tache>> tachesEncadrant(Principal principal) {
        Utilisateur encadrant = candidatureService.getUtilisateurByEmail(principal.getName());
        return ResponseEntity.ok(tacheService.getTachesByEncadrant(encadrant.getId()));
    }

    // Encadrant voit les tâches d'un stagiaire spécifique
    @GetMapping("/stagiaire/{stagiaireId}")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<List<Tache>> tachesDuStagiaire(
            @PathVariable Long stagiaireId) {
        return ResponseEntity.ok(tacheService.getTachesByStagiaire(stagiaireId));
    }

    // Stagiaire ou Encadrant met à jour le statut
    @PutMapping("/{id}/statut")
    @PreAuthorize("hasAuthority('ROLE_STAGIAIRE') or hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<Tache> updateStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        StatusTache statut = StatusTache.valueOf(body.get("statut"));
        return ResponseEntity.ok(tacheService.updateStatut(id, statut));
    }

    // Encadrant modifie une tâche
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<Tache> modifier(
            @PathVariable Long id,
            @RequestBody Tache tache) {
        return ResponseEntity.ok(tacheService.updateTache(id, tache));
    }

    // Encadrant supprime une tâche
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        tacheService.supprimerTache(id);
        return ResponseEntity.noContent().build();
    }
}