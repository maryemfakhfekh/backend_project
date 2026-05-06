package com.asm.gestion_stagiaires.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "analyse_ia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AnalyseIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "candidature_id")
    @JsonBackReference("candidature-analyse")
    private Candidature candidature;

    private Double scoreGlobal;
    private String recommendation;
    private String recommendationLabel;

    private Double scoreCompetences;
    private Double scoreFormation;
    private Double scoreExperience;

    @ElementCollection
    private List<String> competencesMatchees;

    @ElementCollection
    private List<String> competencesManquantes;

    @ElementCollection
    private List<String> educationLines;

    @Column(length = 2000)
    private String justification;

    private Double semanticSimilarity;
    private String auditId;
    private Boolean anonymized = true;
    private LocalDateTime dateAnalyse = LocalDateTime.now();
}