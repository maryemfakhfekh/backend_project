package com.asm.gestion_stagiaires.repositories;

import com.asm.gestion_stagiaires.models.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FiliereRepository extends JpaRepository<Filiere, Long> {
}