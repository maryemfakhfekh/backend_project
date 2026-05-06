package com.asm.gestion_stagiaires.Controller;

import com.asm.gestion_stagiaires.config.JwtUtils;
import com.asm.gestion_stagiaires.models.*;
import com.asm.gestion_stagiaires.repositories.UtilisateurRepository;
import com.asm.gestion_stagiaires.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UtilisateurRepository utilisateurRepository;

    @PostMapping("/register")
    public ResponseEntity<Utilisateur> register(@RequestBody Stagiaire user) {
        return ResponseEntity.ok(authService.inscription(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Utilisateur user = authService.login(loginRequest);
            String token = jwtUtils.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("token", token);
            response.put("email", user.getEmail());
            response.put("nom", user.getNom());
            response.put("prenom", user.getPrenom());
            response.put("telephone", user.getTelephone());

            String role = user.getAuthorities().iterator().next().getAuthority();
            response.put("role", role);

            if (user instanceof Stagiaire stagiaire) {
                response.put("cycle", stagiaire.getCycle());
                response.put("filiere", stagiaire.getFiliere());
                response.put("etablissement", stagiaire.getEtablissement());
                response.put("dateNaissance", stagiaire.getDateNaissance());
            }

            if (user instanceof Encadrant encadrant) {
                response.put("departement", encadrant.getDepartement());
                response.put("specialite", encadrant.getSpecialite());
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/update-profile/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody Map<String, Object> updatedData) {
        return utilisateurRepository.findById(id).map(user -> {
            user.setTelephone((String) updatedData.get("telephone"));

            if (user instanceof Stagiaire stagiaire) {
                if (updatedData.containsKey("etablissement")) {
                    stagiaire.setEtablissement((String) updatedData.get("etablissement"));
                }
                if (updatedData.containsKey("dateNaissance")) {
                    stagiaire.setDateNaissance(java.time.LocalDate.parse((String) updatedData.get("dateNaissance")));
                }
            }

            if (user instanceof Encadrant encadrant) {
                if (updatedData.containsKey("departement")) {
                    encadrant.setDepartement((String) updatedData.get("departement"));
                }
                if (updatedData.containsKey("specialite")) {
                    encadrant.setSpecialite((String) updatedData.get("specialite"));
                }
            }

            utilisateurRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Profil mis à jour !"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/encadrants")
    @PreAuthorize("hasAuthority('ROLE_RH')")
    public ResponseEntity<List<Utilisateur>> getEncadrants() {
        List<Utilisateur> encadrants = utilisateurRepository.findByType(Encadrant.class);
        return ResponseEntity.ok(encadrants);
    }
}