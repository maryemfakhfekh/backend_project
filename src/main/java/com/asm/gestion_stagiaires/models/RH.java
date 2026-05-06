package com.asm.gestion_stagiaires.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "rh")
@DiscriminatorValue("RH")
@Getter
@Setter
@NoArgsConstructor
public class RH extends Utilisateur {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_RH"));
    }
}