package com.asm.gestion_stagiaires.repositories;

import com.asm.gestion_stagiaires.models.DemandeAcces;
import com.asm.gestion_stagiaires.models.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeAccesRepository extends JpaRepository<DemandeAcces, Long> {
    List<DemandeAcces> findByStatut(StatutDemande statut);
    boolean existsByEmail(String email);
}