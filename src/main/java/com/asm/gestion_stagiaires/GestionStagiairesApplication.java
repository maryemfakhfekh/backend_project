package com.asm.gestion_stagiaires;

import com.asm.gestion_stagiaires.models.Admin;
import com.asm.gestion_stagiaires.repositories.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class GestionStagiairesApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionStagiairesApplication.class, args);
	}

	@Bean
	CommandLineRunner initAdmin(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (!utilisateurRepository.existsByEmail("admin@asm.com")) {
				Admin admin = new Admin();
				admin.setNom("Admin");
				admin.setPrenom("System");
				admin.setEmail("admin@asm.com");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setTelephone("00000000");
				utilisateurRepository.save(admin);
				System.out.println("✅ Compte Admin créé : admin@asm.com / admin123");
			}
		};
	}
}