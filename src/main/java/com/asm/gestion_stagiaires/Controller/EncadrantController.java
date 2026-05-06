package com.asm.gestion_stagiaires.Controller;

import com.asm.gestion_stagiaires.models.*;
import com.asm.gestion_stagiaires.repositories.StageRepository;
import com.asm.gestion_stagiaires.repositories.TacheRepository;
import com.asm.gestion_stagiaires.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/encadrants")
@CrossOrigin("*")
public class EncadrantController {

    @Autowired
    private StageRepository stagiaireRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private TacheRepository tacheRepository;

    @GetMapping("/mes-stagiaires")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<List<Stage>> getMesStagiaires(Principal principal) {
        Utilisateur encadrant = utilisateurRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé"));
        List<Stage> stages = stagiaireRepository.findByEncadrantId(encadrant.getId());
        return ResponseEntity.ok(stages);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<?> getDashboard(Principal principal) {
        Utilisateur encadrant = utilisateurRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé"));

        List<Stage> stages = stagiaireRepository.findByEncadrantId(encadrant.getId());
        int stagiairesCount = stages.size();

        List<Tache> taches = tacheRepository.findByEncadrantId(encadrant.getId());
        int tachesCreees = taches.size();
        int tachesEnAttente = (int) taches.stream()
                .filter(t -> t.getStatut() == StatusTache.A_FAIRE)
                .count();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("stagiairesCount", stagiairesCount);
        dashboard.put("tachesCreees", tachesCreees);
        dashboard.put("tachesEnAttente", tachesEnAttente);
        dashboard.put("notificationsEnAttente", 0);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<?> getProfile(Principal principal) {
        Utilisateur user = utilisateurRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé"));

        List<Stage> stages = stagiaireRepository.findByEncadrantId(user.getId());

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("nom", user.getNom());
        profile.put("prenom", user.getPrenom());
        profile.put("email", user.getEmail());
        profile.put("telephone", user.getTelephone());
        profile.put("stagairesCount", stages.size());

        if (user instanceof Encadrant encadrant) {
            profile.put("departement", encadrant.getDepartement());
            profile.put("specialite", encadrant.getSpecialite());
        }

        return ResponseEntity.ok(profile);
    }
}