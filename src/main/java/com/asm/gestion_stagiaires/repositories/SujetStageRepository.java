package com.asm.gestion_stagiaires.repositories;

import com.asm.gestion_stagiaires.models.SujetStage;
import com.asm.gestion_stagiaires.models.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SujetStageRepository extends JpaRepository<SujetStage, Long> {
    List<SujetStage> findByCreateurId(Long rhId);
    List<SujetStage> findByCreateur(Utilisateur createur);
    List<SujetStage> findByCycleCibleId(Long cycleId);
    List<SujetStage> findByEstDisponibleTrue();
    List<SujetStage> findByCycleCibleIdAndFiliereCibleId(Long cycleId, Long filiereId);
}