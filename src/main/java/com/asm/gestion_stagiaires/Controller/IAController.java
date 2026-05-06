package com.asm.gestion_stagiaires.Controller;

import com.asm.gestion_stagiaires.models.AnalyseIA;
import com.asm.gestion_stagiaires.models.QuestionEntretien;
import com.asm.gestion_stagiaires.repositories.AnalyseIARepository;
import com.asm.gestion_stagiaires.repositories.QuestionEntretienRepository;
import com.asm.gestion_stagiaires.services.IAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ia")
@CrossOrigin("*")
public class IAController {

    @Autowired private IAService iaService;
    @Autowired private AnalyseIARepository analyseIARepository;
    @Autowired private QuestionEntretienRepository questionEntretienRepository;

    // RH déclenche l'analyse IA d'une candidature
    @PostMapping("/analyser/{candidatureId}")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<AnalyseIA> analyser(@PathVariable Long candidatureId) {
        AnalyseIA analyse = iaService.analyserCV(candidatureId);
        return ResponseEntity.ok(analyse);
    }

    // RH voit le résultat — score + compétences + formation
    @GetMapping("/analyse/{candidatureId}")
    @PreAuthorize("hasAuthority('ROLE_RH') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AnalyseIA> getAnalyse(@PathVariable Long candidatureId) {
        return analyseIARepository.findByCandidatureId(candidatureId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Encadrant voit les questions d'entretien sur Flutter
    @GetMapping("/questions/{candidatureId}")
    @PreAuthorize("hasAuthority('ROLE_ENCADRANT')")
    public ResponseEntity<List<QuestionEntretien>> getQuestions(
            @PathVariable Long candidatureId) {
        List<QuestionEntretien> questions =
                questionEntretienRepository.findByCandidatureId(candidatureId);
        return ResponseEntity.ok(questions);
    }
}