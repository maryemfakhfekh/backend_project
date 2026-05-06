package com.asm.gestion_stagiaires.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {
    // On définit le chemin vers le dossier que tu as créé
    private final Path root = Paths.get("uploads/cvs");

    public String save(MultipartFile file) {
        try {
            // On crée un nom unique pour ne pas écraser les fichiers
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            // On copie le fichier dans le dossier uploads/cvs
            Files.copy(file.getInputStream(), this.root.resolve(fileName));
            return fileName; // On retourne le nom pour l'enregistrer en base de données
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du stockage du fichier : " + e.getMessage());
        }
    }
}