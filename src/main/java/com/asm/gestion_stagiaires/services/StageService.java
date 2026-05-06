package com.asm.gestion_stagiaires.services;

import com.asm.gestion_stagiaires.models.*;
import com.asm.gestion_stagiaires.repositories.StageRepository;
import com.asm.gestion_stagiaires.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StageService {

    @Autowired private StageRepository stageRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;

    // (ancienne méthode conservée pour compatibilité)
    public Stage creerStage(Utilisateur utilisateur, Candidature candidature) {
        Stage stage = new Stage();
        stage.setUtilisateur(utilisateur);
        stage.setCandidature(candidature);
        stage.setSujet(candidature.getSujet());
        stage.setDateDebut(LocalDate.now());
        stage.setStatusStage(StatusStage.EN_COURS);
        return stageRepository.save(stage);
    }

    // ✅ NOUVEAU : crée le stage avec l'encadrant déjà assigné dans la candidature
    public Stage creerStageDepuisCandidature(Candidature candidature) {
        Stage stage = new Stage();
        stage.setUtilisateur(candidature.getStagiaire());
        stage.setCandidature(candidature);
        stage.setSujet(candidature.getSujet());
        stage.setEncadrant(candidature.getEncadrant()); // ✅ encadrant de l'entretien
        stage.setDateDebut(LocalDate.now());
        stage.setStatusStage(StatusStage.EN_COURS);
        return stageRepository.save(stage);
    }

    public List<Stage> getAllStages() {
        return stageRepository.findAll();
    }

    public Stage getStageByUtilisateurId(Long utilisateurId) {
        return stageRepository.findFirstByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Stage non trouvé"));
    }

    public boolean existsByUtilisateurId(Long utilisateurId) {
        return stageRepository.existsByUtilisateurId(utilisateurId);
    }

    public Stage terminerStage(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stage non trouvé"));
        stage.setStatusStage(StatusStage.TERMINE);
        stage.setDateFin(LocalDate.now());

        Utilisateur stagiaire = stage.getUtilisateur();
        stagiaire.setActif(false);
        utilisateurRepository.save(stagiaire);

        return stageRepository.save(stage);
    }

    public Stage affecterEncadrant(Long stageId, Long encadrantId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Stage non trouvé"));
        Utilisateur encadrant = utilisateurRepository.findById(encadrantId)
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé"));
        stage.setEncadrant(encadrant);
        return stageRepository.save(stage);
    }

    public List<Utilisateur> getEncadrants() {
        return utilisateurRepository.findByType(com.asm.gestion_stagiaires.models.Encadrant.class);
    }
}