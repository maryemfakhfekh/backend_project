package com.asm.gestion_stagiaires.models;

public enum StatusCandidature {
    EN_ATTENTE,             // Candidature soumise, IA en cours / RH n'a pas encore agi
    EN_ENTRETIEN,           // RH a planifié l'entretien avec un encadrant
    VALIDEE_ENCADRANT,      // Encadrant a validé après l'entretien (en attente décision RH)
    REFUSEE_ENCADRANT,      // Encadrant a refusé après l'entretien
    ACCEPTE,                // RH a définitivement accepté → stage créé
    REFUSEE                 // Refus définitif (par RH)
}