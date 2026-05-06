package com.asm.gestion_stagiaires.repositories;

import com.asm.gestion_stagiaires.models.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByStagiaireId(Long stagiaireId);
}