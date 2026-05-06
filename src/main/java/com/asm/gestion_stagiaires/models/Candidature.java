package com.asm.gestion_stagiaires.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cvPath;

    private Double scoreMatchingIA;

    @ElementCollection
    private List<String> competencesExtraites;

    @Enumerated(EnumType.STRING)
    private StatusCandidature statut = StatusCandidature.EN_ATTENTE;

    private LocalDate dateDepot = LocalDate.now();

    private LocalDateTime dateEntretien;

    // Commentaire de l'encadrant après entretien
    @Column(length = 1000)
    private String commentaireEncadrant;

    @ManyToOne
    @JoinColumn(name = "stagiaire_id")
    private Utilisateur stagiaire;

    @ManyToOne
    @JoinColumn(name = "sujet_id")
    private SujetStage sujet;

    // Encadrant assigné par le RH
    @ManyToOne
    @JoinColumn(name = "encadrant_id")
    private Utilisateur encadrant;

    @OneToOne(mappedBy = "candidature", cascade = CascadeType.ALL)
    @JsonManagedReference("candidature-analyse")
    private AnalyseIA analyseIA;

    @OneToMany(mappedBy = "candidature", cascade = CascadeType.ALL)
    @JsonManagedReference("candidature-questions")
    private List<QuestionEntretien> questionsEntretien;
}