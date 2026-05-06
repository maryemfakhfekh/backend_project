package com.asm.gestion_stagiaires.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_entretien")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class QuestionEntretien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidature_id")
    @JsonBackReference("candidature-questions")
    private Candidature candidature;

    private String category;

    @Column(length = 1000)
    private String question;

    private String source;
}