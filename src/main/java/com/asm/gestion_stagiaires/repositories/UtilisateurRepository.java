package com.asm.gestion_stagiaires.repositories;

import com.asm.gestion_stagiaires.models.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM Utilisateur u WHERE TYPE(u) = :type")
    List<Utilisateur> findByType(Class<? extends Utilisateur> type);
}