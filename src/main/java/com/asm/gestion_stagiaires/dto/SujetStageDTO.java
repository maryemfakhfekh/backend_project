package com.asm.gestion_stagiaires.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SujetStageDTO {
    private Long id;
    private String titre;
    private String description;
    private Long filiereId;
    private String filiereNom;
    private Long cycleId;
    private String cycleNom;
    private List<String> competencesCibles;
    private LocalDate datePublication;
    private Boolean estDisponible;
    private Long createurId;
    private String createurNom;
}