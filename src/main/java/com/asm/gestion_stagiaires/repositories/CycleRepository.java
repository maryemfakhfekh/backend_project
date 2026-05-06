package com.asm.gestion_stagiaires.repositories;

import com.asm.gestion_stagiaires.models.Cycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CycleRepository extends JpaRepository<Cycle, Long> {
}
