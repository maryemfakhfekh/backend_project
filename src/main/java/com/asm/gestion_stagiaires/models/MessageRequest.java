// src/main/java/com/asm/gestion_stagiaires/models/MessageRequest.java

package com.asm.gestion_stagiaires.models;

import lombok.Data;

@Data
public class MessageRequest {
    private Long   expediteurId;
    private Long   destinataireId;
    private String contenu;
}