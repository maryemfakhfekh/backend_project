package com.asm.gestion_stagiaires.repositories;

import com.asm.gestion_stagiaires.models.QuestionEntretien;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionEntretienRepository extends JpaRepository<QuestionEntretien, Long> {
    List<QuestionEntretien> findByCandidatureId(Long candidatureId);
}