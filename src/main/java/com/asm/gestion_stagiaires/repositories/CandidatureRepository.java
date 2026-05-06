package com.asm.gestion_stagiaires.repositories;

import com.asm.gestion_stagiaires.models.Candidature;
import com.asm.gestion_stagiaires.models.StatusCandidature;
import com.asm.gestion_stagiaires.models.SujetStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    List<Candidature> findByStagiaireId(Long stagiaireId);
    List<Candidature> findByStatut(StatusCandidature statut);
    List<Candidature> findBySujetIn(List<SujetStage> sujets);
    boolean existsByStagiaireIdAndStatut(Long stagiaireId, StatusCandidature statut);

    // ✅ Pour que l'encadrant voit les candidatures qui lui sont assignées
    List<Candidature> findByEncadrantId(Long encadrantId);

    // ✅ Pour que l'encadrant voit ses candidatures par statut (ses entretiens à faire)
    List<Candidature> findByEncadrantIdAndStatut(Long encadrantId, StatusCandidature statut);


}