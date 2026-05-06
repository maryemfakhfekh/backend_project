package com.asm.gestion_stagiaires.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "demande_acces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandeAcces {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String email;

    private String telephone;

    // ✅ Mot de passe choisi par l'utilisateur lors de la demande
    @Column(nullable = false)
    private String password;

    @Column(name = "role_souhaite", nullable = false)
    private String roleSouhaite; // "RH" ou "ENCADRANT"

    // Champs spécifiques encadrant
    private String departement;
    private String specialite;

    @Enumerated(EnumType.STRING)
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    @Column(name = "date_demande")
    private LocalDate dateDemande = LocalDate.now();
}