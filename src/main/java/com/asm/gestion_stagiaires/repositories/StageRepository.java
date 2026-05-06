package com.asm.gestion_stagiaires.repositories;

import com.asm.gestion_stagiaires.models.Stage;
import com.asm.gestion_stagiaires.models.StatusStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {

    // ✅ Pour StageService.getStageByUtilisateurId (un seul stage par utilisateur attendu)
    Optional<Stage> findFirstByUtilisateurId(Long utilisateurId);

    // ✅ Pour CandidatureService (liste, au cas où)
    List<Stage> findAllByUtilisateurId(Long utilisateurId);

    List<Stage> findByStatusStage(StatusStage statusStage);

    boolean existsByUtilisateurId(Long utilisateurId);

    List<Stage> findByEncadrantId(Long encadrantId);
}