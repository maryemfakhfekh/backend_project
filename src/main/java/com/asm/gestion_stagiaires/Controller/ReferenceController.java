package com.asm.gestion_stagiaires.Controller;

import com.asm.gestion_stagiaires.models.Cycle;
import com.asm.gestion_stagiaires.models.Filiere;
import com.asm.gestion_stagiaires.repositories.CycleRepository;
import com.asm.gestion_stagiaires.repositories.FiliereRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/references")
@CrossOrigin("*")
public class ReferenceController {

    @Autowired
    private FiliereRepository filiereRepository;

    @Autowired
    private CycleRepository cycleRepository;

    // ===== FILIÈRES =====

    @GetMapping("/filieres")
    public ResponseEntity<List<Filiere>> getAllFilieres() {
        return ResponseEntity.ok(filiereRepository.findAll());
    }

    @PostMapping("/filieres")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> ajouterFiliere(@RequestBody Filiere filiere) {
        if (filiereRepository.findAll().stream().anyMatch(f -> f.getNom().equalsIgnoreCase(filiere.getNom()))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cette filière existe déjà"));
        }
        return ResponseEntity.ok(filiereRepository.save(filiere));
    }

    @PutMapping("/filieres/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> modifierFiliere(@PathVariable Long id, @RequestBody Filiere filiere) {
        return filiereRepository.findById(id).map(f -> {
            f.setNom(filiere.getNom());
            return ResponseEntity.ok(filiereRepository.save(f));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/filieres/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> supprimerFiliere(@PathVariable Long id) {
        filiereRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Filière supprimée"));
    }

    // ===== CYCLES =====

    @GetMapping("/cycles")
    public ResponseEntity<List<Cycle>> getAllCycles() {
        return ResponseEntity.ok(cycleRepository.findAll());
    }

    @PostMapping("/cycles")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> ajouterCycle(@RequestBody Cycle cycle) {
        if (cycleRepository.findAll().stream().anyMatch(c -> c.getNom().equalsIgnoreCase(cycle.getNom()))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ce cycle existe déjà"));
        }
        return ResponseEntity.ok(cycleRepository.save(cycle));
    }

    @PutMapping("/cycles/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> modifierCycle(@PathVariable Long id, @RequestBody Cycle cycle) {
        return cycleRepository.findById(id).map(c -> {
            c.setNom(cycle.getNom());
            return ResponseEntity.ok(cycleRepository.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/cycles/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> supprimerCycle(@PathVariable Long id) {
        cycleRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Cycle supprimé"));
    }
}