package com.asm.gestion_stagiaires.services;

import com.asm.gestion_stagiaires.models.LoginRequest;
import com.asm.gestion_stagiaires.models.Stagiaire;
import com.asm.gestion_stagiaires.models.Utilisateur;
import com.asm.gestion_stagiaires.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UtilisateurRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Utilisateur inscription(Stagiaire user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Cet email existe déjà");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Utilisateur login(LoginRequest loginRequest) {
        Optional<Utilisateur> userOpt = userRepository.findByEmail(loginRequest.getEmail());

        if (userOpt.isPresent()) {
            Utilisateur user = userOpt.get();
            if (!user.isEnabled()) {
                throw new RuntimeException("Votre compte est désactivé. Contactez l'administrateur.");
            }
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return user;
            }
        }

        throw new RuntimeException("Email ou mot de passe incorrect");
    }
}