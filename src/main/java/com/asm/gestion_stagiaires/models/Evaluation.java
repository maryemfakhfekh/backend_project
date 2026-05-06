package com.asm.gestion_stagiaires.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "evaluation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double note;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    private LocalDate dateEvaluation = LocalDate.now();

    @OneToOne
    @JoinColumn(name = "stagiaire_id")
    private Stage stagiaire;

    @ManyToOne
    @JoinColumn(name = "encadrant_id")
    private Utilisateur encadrant;
}