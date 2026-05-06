package com.asm.gestion_stagiaires.services;

import com.asm.gestion_stagiaires.models.*;
import com.asm.gestion_stagiaires.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RapportService {

    @Autowired private RapportRepository rapportRepository;
    @Autowired private StageRepository stagiaireRepository;

    public Rapport deposerRapport(Long stagiaireId, String fichierPath) {
        Stage stagiaire = stagiaireRepository.findById(stagiaireId)
                .orElseThrow(() -> new RuntimeException("Stagiaire non trouvé"));

        Rapport rapport = rapportRepository
                .findByStagiaireId(stagiaireId)
                .orElse(new Rapport());

        rapport.setStagiaire(stagiaire);
        rapport.setFichierPath(fichierPath);
        rapport.setDateDepot(LocalDate.now());
        return rapportRepository.save(rapport);
    }

    public Rapport getRapportByStagiaire(Long stagiaireId) {
        return rapportRepository.findByStagiaireId(stagiaireId)
                .orElseThrow(() -> new RuntimeException("Rapport non trouvé"));
    }

    public List<Rapport> getAllRapports() {
        return rapportRepository.findAll();
    }
}