package com.asm.gestion_stagiaires.repositories;

import com.asm.gestion_stagiaires.models.Rapport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RapportRepository extends JpaRepository<Rapport, Long> {
    Optional<Rapport> findByStagiaireId(Long stagiaireId);
}