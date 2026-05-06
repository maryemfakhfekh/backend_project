package com.asm.gestion_stagiaires.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "encadrant")
@DiscriminatorValue("ENCADRANT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Encadrant extends Utilisateur {

    @Column(name = "departement")
    private String departement;

    @Column(name = "specialite")
    private String specialite;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_ENCADRANT"));
    }
}