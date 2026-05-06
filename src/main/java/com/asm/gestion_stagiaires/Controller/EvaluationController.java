package com.asm.gestion_stagiaires.Controller;

import com.asm.gestion_stagiaires.models.Evaluation;
import com.asm.gestion_stagiaires.models.Utilisateur;
import com.asm.gestion_stagiaires.services.CandidatureService;
import com.asm.gestion_stagiaires.services.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluations")
@CrossOrigin("*")
public class EvaluationController {

    @Autowired private EvaluationService evaluationService;
    @Autowired private CandidatureService candidatureService;

    // ✅ RH et Admin voient toutes les évaluations — SecurityConfig gère les droits
    @GetMapping
    public ResponseEntity<List<Evaluation>> getAllEvaluations() {
        return ResponseEntity.ok(evaluationService.getAllEvaluations());
    }

    // ✅ Encadrant crée ou modifie une évaluation
    @PostMapping("/stagiaire/{stagiaireId}")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<Evaluation> evaluer(
            @PathVariable Long stagiaireId,
            @RequestBody Map<String, Object> body,
            Principal principal) {
        Utilisateur encadrant = candidatureService.getUtilisateurByEmail(principal.getName());
        Double note = Double.parseDouble(body.get("note").toString());
        String commentaire = body.get("commentaire").toString();
        return ResponseEntity.ok(
                evaluationService.creerEvaluation(
                        stagiaireId, encadrant.getId(), note, commentaire));
    }

    // ✅ Voir l'évaluation d'un stagiaire
    @GetMapping("/stagiaire/{stagiaireId}")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT') or hasAuthority('ROLE_RH') or hasAuthority('ROLE_STAGIAIRE')")
    public ResponseEntity<Evaluation> getEvaluation(@PathVariable Long stagiaireId) {
        return ResponseEntity.ok(evaluationService.getEvaluationByStagiaire(stagiaireId));
    }
}