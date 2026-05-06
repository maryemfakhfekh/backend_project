package com.asm.gestion_stagiaires.repositories;

import com.asm.gestion_stagiaires.models.Tache;
import com.asm.gestion_stagiaires.models.StatusTache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TacheRepository extends JpaRepository<Tache, Long> {
    List<Tache> findByStagiaireId(Long stagiaireId);
    List<Tache> findByEncadrantId(Long encadrantId);
    List<Tache> findByStagiaireIdAndStatut(Long stagiaireId, StatusTache statut);
}