package com.asm.gestion_stagiaires.Controller;

import com.asm.gestion_stagiaires.models.Rapport;
import com.asm.gestion_stagiaires.models.Utilisateur;
import com.asm.gestion_stagiaires.services.CandidatureService;
import com.asm.gestion_stagiaires.services.FileStorageService;
import com.asm.gestion_stagiaires.services.RapportService;
import com.asm.gestion_stagiaires.services.StageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/rapports")
@CrossOrigin("*")
public class RapportController {

    @Autowired private RapportService rapportService;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private CandidatureService candidatureService;
    @Autowired private StageService stagiaireService;

    // Stagiaire dépose son rapport
    @PostMapping("/deposer")
    @PreAuthorize("hasAuthority('ROLE_STAGIAIRE')")
    public ResponseEntity<Rapport> deposer(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        Utilisateur utilisateur = candidatureService.getUtilisateurByEmail(principal.getName());
        var stagiaire = stagiaireService.getStageByUtilisateurId(utilisateur.getId());
        String path = fileStorageService.save(file);
        return ResponseEntity.ok(rapportService.deposerRapport(stagiaire.getId(), path));
    }

    // Stagiaire voit son rapport
    @GetMapping("/mon-rapport")
    @PreAuthorize("hasAuthority('ROLE_STAGIAIRE')")
    public ResponseEntity<Rapport> monRapport(Principal principal) {
        Utilisateur utilisateur = candidatureService.getUtilisateurByEmail(principal.getName());
        var stagiaire = stagiaireService.getStageByUtilisateurId(utilisateur.getId());
        return ResponseEntity.ok(rapportService.getRapportByStagiaire(stagiaire.getId()));
    }

    // RH et Encadrant voient tous les rapports
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_RH') or hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<List<Rapport>> tous() {
        return ResponseEntity.ok(rapportService.getAllRapports());
    }

    // Encadrant voit le rapport d'un stagiaire spécifique
    @GetMapping("/stagiaire/{stagiaireId}")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT') or hasAuthority('ROLE_RH')")
    public ResponseEntity<Rapport> rapportDuStagiaire(
            @PathVariable Long stagiaireId) {
        return ResponseEntity.ok(rapportService.getRapportByStagiaire(stagiaireId));
    }
    @GetMapping("/fichier/{fileName:.+}")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT') or hasAuthority('ROLE_RH')")
    public ResponseEntity<Resource> getFichier(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get("uploads/cvs").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = fileName.endsWith(".pdf")
                    ? "application/pdf"
                    : "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}