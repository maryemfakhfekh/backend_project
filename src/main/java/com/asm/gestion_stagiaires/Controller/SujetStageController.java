package com.asm.gestion_stagiaires.Controller;

import com.asm.gestion_stagiaires.dto.SujetStageDTO;
import com.asm.gestion_stagiaires.models.Cycle;
import com.asm.gestion_stagiaires.models.Filiere;
import com.asm.gestion_stagiaires.models.SujetStage;
import com.asm.gestion_stagiaires.models.Utilisateur;
import com.asm.gestion_stagiaires.repositories.CycleRepository;
import com.asm.gestion_stagiaires.repositories.FiliereRepository;
import com.asm.gestion_stagiaires.repositories.SujetStageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sujets")
@CrossOrigin("*")
public class SujetStageController {

    @Autowired private SujetStageRepository sujetStageRepository;
    @Autowired private FiliereRepository filiereRepository;
    @Autowired private CycleRepository cycleRepository;

    private SujetStageDTO toDTO(SujetStage s) {
        SujetStageDTO dto = new SujetStageDTO();
        dto.setId(s.getId());
        dto.setTitre(s.getTitre());
        dto.setDescription(s.getDescription());
        if (s.getFiliereCible() != null) {
            dto.setFiliereId(s.getFiliereCible().getId());
            dto.setFiliereNom(s.getFiliereCible().getNom());
        }
        if (s.getCycleCible() != null) {
            dto.setCycleId(s.getCycleCible().getId());
            dto.setCycleNom(s.getCycleCible().getNom());
        }
        dto.setCompetencesCibles(s.getCompetencesCibles());
        dto.setDatePublication(s.getDatePublication());
        dto.setEstDisponible(s.getEstDisponible());
        if (s.getCreateur() != null) {
            dto.setCreateurId(s.getCreateur().getId());
            dto.setCreateurNom(s.getCreateur().getNom() + " " + s.getCreateur().getPrenom());
        }
        return dto;
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<SujetStageDTO>> voirSujetsDisponibles() {
        List<SujetStageDTO> offres = sujetStageRepository
                .findByEstDisponibleTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(offres);
    }

    @PostMapping("/publier")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<?> creerSujet(@RequestBody Map<String, Object> body,
                                        @AuthenticationPrincipal Utilisateur createur) {
        if (createur == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        SujetStage sujet = new SujetStage();
        sujet.setTitre((String) body.get("titre"));
        sujet.setDescription((String) body.get("description"));
        sujet.setCompetencesCibles((List<String>) body.get("competencesCibles"));
        sujet.setCreateur(createur);

        if (body.containsKey("filiereId")) {
            Long filiereId = Long.valueOf(body.get("filiereId").toString());
            Filiere filiere = filiereRepository.findById(filiereId)
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée"));
            sujet.setFiliereCible(filiere);
        }

        if (body.containsKey("cycleId")) {
            Long cycleId = Long.valueOf(body.get("cycleId").toString());
            Cycle cycle = cycleRepository.findById(cycleId)
                    .orElseThrow(() -> new RuntimeException("Cycle non trouvé"));
            sujet.setCycleCible(cycle);
        }

        SujetStage saved = sujetStageRepository.save(sujet);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    @GetMapping("/mes-offres")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<List<SujetStageDTO>> voirMesSujets(@AuthenticationPrincipal Utilisateur createur) {
        if (createur == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<SujetStageDTO> offres = sujetStageRepository.findByCreateurId(createur.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(offres);
    }

    @GetMapping
    public ResponseEntity<List<SujetStageDTO>> voirLesSujets(
            @RequestParam(required = false) Long cycleId) {
        List<SujetStage> offres;
        if (cycleId != null) {
            offres = sujetStageRepository.findByCycleCibleId(cycleId);
        } else {
            offres = sujetStageRepository.findAll();
        }
        return ResponseEntity.ok(offres.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SujetStageDTO> voirSujetParId(@PathVariable Long id) {
        return sujetStageRepository.findById(id)
                .map(s -> ResponseEntity.ok(toDTO(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<?> modifierSujet(@PathVariable Long id,
                                           @RequestBody Map<String, Object> body,
                                           @AuthenticationPrincipal Utilisateur createur) {
        if (createur == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        var optionalSujet = sujetStageRepository.findById(id);
        if (optionalSujet.isEmpty()) return ResponseEntity.notFound().build();
        var sujet = optionalSujet.get();
        if (!sujet.getCreateur().getId().equals(createur.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (body.containsKey("titre")) sujet.setTitre((String) body.get("titre"));
        if (body.containsKey("description")) sujet.setDescription((String) body.get("description"));
        if (body.containsKey("competencesCibles")) sujet.setCompetencesCibles((List<String>) body.get("competencesCibles"));
        if (body.containsKey("estDisponible")) sujet.setEstDisponible((Boolean) body.get("estDisponible"));

        if (body.containsKey("filiereId")) {
            Long filiereId = Long.valueOf(body.get("filiereId").toString());
            sujet.setFiliereCible(filiereRepository.findById(filiereId)
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée")));
        }

        if (body.containsKey("cycleId")) {
            Long cycleId = Long.valueOf(body.get("cycleId").toString());
            sujet.setCycleCible(cycleRepository.findById(cycleId)
                    .orElseThrow(() -> new RuntimeException("Cycle non trouvé")));
        }

        return ResponseEntity.ok(toDTO(sujetStageRepository.save(sujet)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<Void> supprimerSujet(@PathVariable Long id,
                                               @AuthenticationPrincipal Utilisateur createur) {
        if (createur == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        var optionalSujet = sujetStageRepository.findById(id);
        if (optionalSujet.isEmpty()) return ResponseEntity.notFound().build();
        var sujet = optionalSujet.get();
        if (!sujet.getCreateur().getId().equals(createur.getId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        sujetStageRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}