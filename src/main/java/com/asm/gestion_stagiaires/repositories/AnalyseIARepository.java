package com.asm.gestion_stagiaires.repositories;

import com.asm.gestion_stagiaires.models.AnalyseIA;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AnalyseIARepository extends JpaRepository<AnalyseIA, Long> {
    Optional<AnalyseIA> findByCandidatureId(Long candidatureId);
}