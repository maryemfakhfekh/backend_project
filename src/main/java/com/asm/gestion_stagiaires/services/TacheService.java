package com.asm.gestion_stagiaires.services;

import com.asm.gestion_stagiaires.models.*;
import com.asm.gestion_stagiaires.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TacheService {

    @Autowired private TacheRepository tacheRepository;
    @Autowired private StageRepository stagiaireRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;

    public Tache creerTache(Long stagiaireId, Long encadrantId, Tache tache) {
        Stage stagiaire = stagiaireRepository.findById(stagiaireId)
                .orElseThrow(() -> new RuntimeException("Stagiaire non trouvé"));
        Utilisateur encadrant = utilisateurRepository.findById(encadrantId)
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé"));
        tache.setStagiaire(stagiaire);
        tache.setEncadrant(encadrant);
        return tacheRepository.save(tache);
    }

    public List<Tache> getTachesByStagiaire(Long stagiaireId) {
        return tacheRepository.findByStagiaireId(stagiaireId);
    }

    public List<Tache> getTachesByEncadrant(Long encadrantId) {
        return tacheRepository.findByEncadrantId(encadrantId);
    }

    public Tache updateStatut(Long id, StatusTache statut) {
        Tache tache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        tache.setStatut(statut);
        return tacheRepository.save(tache);
    }

    public Tache updateTache(Long id, Tache updated) {
        Tache tache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        tache.setTitre(updated.getTitre());
        tache.setDescription(updated.getDescription());
        tache.setPriorite(updated.getPriorite());
        tache.setDateEcheance(updated.getDateEcheance());
        return tacheRepository.save(tache);
    }

    public void supprimerTache(Long id) {
        tacheRepository.deleteById(id);
    }
}