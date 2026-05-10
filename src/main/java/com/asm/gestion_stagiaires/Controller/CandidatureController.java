package com.asm.gestion_stagiaires.Controller;

import com.asm.gestion_stagiaires.models.Candidature;
import com.asm.gestion_stagiaires.models.Encadrant;
import com.asm.gestion_stagiaires.models.Utilisateur;
import com.asm.gestion_stagiaires.repositories.UtilisateurRepository;
import com.asm.gestion_stagiaires.services.CandidatureService;
import com.asm.gestion_stagiaires.services.FileStorageService;
import com.asm.gestion_stagiaires.services.IAService;
import com.asm.gestion_stagiaires.services.StageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidatures")
@CrossOrigin("*")
public class CandidatureController {

    @Autowired private CandidatureService candidatureService;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private StageService stagiaireService;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private IAService iaService;

    // ✅ Liste des encadrants pour le RH
    @GetMapping("/encadrants")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<List<Utilisateur>> getEncadrants() {
        List<Utilisateur> encadrants = utilisateurRepository.findByType(Encadrant.class);
        return ResponseEntity.ok(encadrants);
    }

    // ✅ Stagiaire soumet sa candidature
    @PostMapping("/postuler")
    @PreAuthorize("hasAuthority('ROLE_STAGIAIRE')")
    public ResponseEntity<Candidature> postuler(
            @RequestParam("File") MultipartFile file,
            @RequestParam("sujetId") Long sujetId,
            Principal principal) {

        String fileName = fileStorageService.save(file);
        Candidature candidature = new Candidature();
        candidature.setCvPath(fileName);

        return ResponseEntity.ok(
                candidatureService.saveCandidature(
                        candidature, sujetId, principal.getName())
        );
    }

    // ✅ Liste des candidatures
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_RH') or hasAuthority('ROLE_STAGIAIRE') or hasAuthority('ROLE_ADMIN')")
    public List<Candidature> voirCandidatures(
            @RequestParam(required = false) Long stagiaireId,
            Principal principal) {

        if (stagiaireId != null) {
            return candidatureService.getCandidaturesByStagiaire(stagiaireId);
        }
        if (principal.getName().equals("admin@asm.com")) {
            return candidatureService.getAllCandidatures();
        }
        return candidatureService.getAllCandidatures();
    }

    // ✅ RH : Accepter définitivement (après commentaire encadrant)
    @PutMapping("/{id}/accepter")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<Candidature> accepter(@PathVariable Long id) {
        Candidature candidature = candidatureService.accepterCandidature(id);
        stagiaireService.creerStageDepuisCandidature(candidature);
        return ResponseEntity.ok(candidature);
    }

    // ✅ RH : Refuser définitivement
    @PutMapping("/{id}/refuser")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<Candidature> refuser(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.refuserCandidature(id));
    }

    // ✅ RH : Planifier entretien avec encadrant
    @PutMapping("/{id}/entretien")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<Candidature> planifierEntretien(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        LocalDateTime dateEntretien = LocalDateTime.parse(body.get("dateEntretien"));
        Long encadrantId = Long.valueOf(body.get("encadrantId"));
        return ResponseEntity.ok(
                candidatureService.planifierEntretien(id, dateEntretien, encadrantId)
        );
    }

    // ✅ ENCADRANT : Marquer entretien comme réalisé + commentaire obligatoire
    @PutMapping("/{id}/valider-encadrant")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<Candidature> marquerEntretienRealise(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Principal principal) {

        String commentaire = body.get("commentaire");
        if (commentaire == null || commentaire.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                candidatureService.marquerEntretienRealise(
                        id, principal.getName(), commentaire)
        );
    }

    // ✅ ENCADRANT : Voir ses entretiens
    @GetMapping("/mes-entretiens")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<List<Candidature>> mesEntretiens(Principal principal) {
        return ResponseEntity.ok(
                candidatureService.getMesEntretiens(principal.getName())
        );
    }

    // ✅ RH : Supprimer une candidature
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        candidatureService.supprimerCandidature(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ STAGIAIRE : Vérifier s'il a une candidature acceptée
    @GetMapping("/has-accepted")
    @PreAuthorize("hasAuthority('ROLE_STAGIAIRE')")
    public ResponseEntity<Boolean> hasAcceptedCandidature(Principal principal) {
        Utilisateur utilisateur = candidatureService
                .getUtilisateurByEmail(principal.getName());
        boolean hasAccepted = candidatureService
                .hasAcceptedCandidature(utilisateur.getId());
        return ResponseEntity.ok(hasAccepted);
    }

    // ✅ RH : Déclencher analyse IA manuellement
    @PostMapping("/{id}/analyser")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<?> analyserCandidature(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(iaService.analyserCV(id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}