package com.asm.gestion_stagiaires.Controller;

import com.asm.gestion_stagiaires.models.Stage;
import com.asm.gestion_stagiaires.models.Utilisateur;
import com.asm.gestion_stagiaires.services.CandidatureService;
import com.asm.gestion_stagiaires.services.StageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/stages")
@CrossOrigin("*")
public class StageController {

    @Autowired
    private StageService stageService;

    @Autowired
    private CandidatureService candidatureService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_RH') or hasAuthority('ROLE_ADMIN')")  // ✅
    public List<Stage> getAllStages() {
        return stageService.getAllStages();
    }

    @GetMapping("/mon-dossier")
    @PreAuthorize("hasAuthority('ROLE_STAGIAIRE')")
    public ResponseEntity<Stage> getMonDossier(Principal principal) {
        Utilisateur utilisateur = candidatureService.getUtilisateurByEmail(principal.getName());
        Stage stage = stageService.getStageByUtilisateurId(utilisateur.getId());
        return ResponseEntity.ok(stage);
    }

    @GetMapping("/has-dossier")
    @PreAuthorize("hasAuthority('ROLE_STAGIAIRE')")
    public ResponseEntity<Boolean> hasDossier(Principal principal) {
        Utilisateur utilisateur = candidatureService.getUtilisateurByEmail(principal.getName());
        boolean exists = stageService.existsByUtilisateurId(utilisateur.getId());
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/encadrants")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public List<Utilisateur> getEncadrants() {
        return stageService.getEncadrants();
    }

    @PutMapping("/{id}/affecter-encadrant")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<Stage> affecterEncadrant(
            @PathVariable Long id,
            @RequestParam Long encadrantId) {
        return ResponseEntity.ok(stageService.affecterEncadrant(id, encadrantId));
    }

    @PutMapping("/{id}/terminer")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<Stage> terminerStage(@PathVariable Long id) {
        return ResponseEntity.ok(stageService.terminerStage(id));
    }
}