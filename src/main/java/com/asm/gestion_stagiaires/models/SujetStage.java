package com.asm.gestion_stagiaires.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SujetStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "filiere_id")
    private Filiere filiereCible;

    @ManyToOne
    @JoinColumn(name = "cycle_id")
    private Cycle cycleCible;

    @ElementCollection
    private List<String> competencesCibles;

    private LocalDate datePublication = LocalDate.now();

    private Boolean estDisponible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rh")
    @JsonIgnore
    private Utilisateur createur;
}