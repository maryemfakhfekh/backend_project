package com.asm.gestion_stagiaires.services;

import com.asm.gestion_stagiaires.models.*;
import com.asm.gestion_stagiaires.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EvaluationService {

    @Autowired private EvaluationRepository evaluationRepository;
    @Autowired private StageRepository stagiaireRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;

    // ✅ Toutes les évaluations — pour RH et Admin
    public List<Evaluation> getAllEvaluations() {
        return evaluationRepository.findAll();
    }

    public Evaluation creerEvaluation(Long stagiaireId, Long encadrantId,
                                      Double note, String commentaire) {
        Stage stagiaire = stagiaireRepository.findById(stagiaireId)
                .orElseThrow(() -> new RuntimeException("Stage non trouvé"));
        Utilisateur encadrant = utilisateurRepository.findById(encadrantId)
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé"));

        Evaluation evaluation = evaluationRepository
                .findByStagiaireId(stagiaireId)
                .orElse(new Evaluation());

        evaluation.setStagiaire(stagiaire);
        evaluation.setEncadrant(encadrant);
        evaluation.setNote(note);
        evaluation.setCommentaire(commentaire);
        evaluation.setDateEvaluation(LocalDate.now());
        return evaluationRepository.save(evaluation);
    }

    public Evaluation getEvaluationByStagiaire(Long stagiaireId) {
        return evaluationRepository.findByStagiaireId(stagiaireId)
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));
    }
}