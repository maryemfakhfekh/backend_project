package com.asm.gestion_stagiaires.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "rapport")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fichier_path")
    private String fichierPath;

    private LocalDate dateDepot = LocalDate.now();

    @OneToOne
    @JoinColumn(name = "stagiaire_id")
    private Stage stagiaire;
}