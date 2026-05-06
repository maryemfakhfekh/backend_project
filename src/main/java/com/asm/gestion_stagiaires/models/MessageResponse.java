// src/main/java/com/asm/gestion_stagiaires/models/MessageResponse.java

package com.asm.gestion_stagiaires.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MessageResponse {
    private Long          id;
    private Long          expediteurId;
    private Long          destinataireId;
    private String        contenu;
    private LocalDateTime dateEnvoi;
    private boolean       lu;

    public static MessageResponse fromEntity(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getExpediteur().getId(),
                m.getDestinataire().getId(),
                m.getContenu(),
                m.getDateEnvoi(),
                m.isLu()
        );
    }
}