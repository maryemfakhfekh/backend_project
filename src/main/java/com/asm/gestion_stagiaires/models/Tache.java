package com.asm.gestion_stagiaires.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "tache")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private StatusTache statut = StatusTache.A_FAIRE;

    @Enumerated(EnumType.STRING)
    private PrioriteTache priorite = PrioriteTache.MOYENNE;

    private LocalDate dateEcheance;

    private LocalDate dateCreation = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "stagiaire_id", nullable = false)
    private Stage stagiaire;

    @ManyToOne
    @JoinColumn(name = "encadrant_id", nullable = false)
    private Utilisateur encadrant;
}