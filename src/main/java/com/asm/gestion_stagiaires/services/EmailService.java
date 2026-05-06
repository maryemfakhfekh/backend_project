package com.asm.gestion_stagiaires.services;

import com.asm.gestion_stagiaires.models.Utilisateur;
import com.asm.gestion_stagiaires.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    @Autowired private JavaMailSender mailSender;
    @Autowired private UtilisateurRepository utilisateurRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    // ===== DEMANDES D'ACCÈS =====

    public void envoyerEmailActivation(String destinataire, String nom, String prenom, String role) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(destinataire);
        message.setSubject("ASM - Votre compte a été activé");
        message.setText(
                "Bonjour " + prenom + " " + nom + ",\n\n"
                        + "Bonne nouvelle ! Votre demande d'accès en tant que " + role + " a été acceptée.\n\n"
                        + "Votre compte est désormais actif. Vous pouvez vous connecter à la plateforme avec "
                        + "l'email et le mot de passe que vous avez fournis lors de votre inscription.\n\n"
                        + "Lien de la plateforme : http://localhost:8085\n\n"
                        + "Cordialement,\nL'équipe ASM"
        );
        mailSender.send(message);
    }

    public void envoyerEmailRefus(String destinataire, String nom, String prenom, String role) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(destinataire);
        message.setSubject("ASM - Réponse à votre demande d'accès");
        message.setText(
                "Bonjour " + prenom + " " + nom + ",\n\n"
                        + "Nous vous remercions pour votre demande d'accès en tant que " + role + ".\n\n"
                        + "Après examen, nous sommes au regret de vous informer que votre demande "
                        + "n'a pas été acceptée.\n\n"
                        + "Cordialement,\nL'équipe ASM"
        );
        mailSender.send(message);
    }

    // ===== CANDIDATURES (stagiaire) =====

    public void envoyerEmailCandidatureAcceptee(String destinataire, String nom, String prenom, String sujetStage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(destinataire);
        message.setSubject("ASM - Candidature acceptée");
        message.setText(
                "Bonjour " + prenom + " " + nom + ",\n\n"
                        + "Félicitations ! Votre candidature pour le stage \"" + sujetStage
                        + "\" a été définitivement acceptée.\n\n"
                        + "Votre stage va débuter prochainement. Vous pouvez consulter les détails sur la plateforme.\n\n"
                        + "Lien de la plateforme : http://localhost:8085\n\n"
                        + "Cordialement,\nL'équipe ASM"
        );
        mailSender.send(message);
    }

    public void envoyerEmailCandidatureRefusee(String destinataire, String nom, String prenom, String sujetStage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(destinataire);
        message.setSubject("ASM - Réponse à votre candidature");
        message.setText(
                "Bonjour " + prenom + " " + nom + ",\n\n"
                        + "Nous vous remercions pour votre candidature au stage \"" + sujetStage + "\".\n\n"
                        + "Après étude de votre dossier, nous sommes au regret de vous informer "
                        + "que votre candidature n'a pas été retenue.\n\n"
                        + "Cordialement,\nL'équipe ASM"
        );
        mailSender.send(message);
    }

    // ===== ENTRETIEN =====

    public void envoyerEmailEntretien(String destinataire, String nomDestinataire, String prenomDestinataire,
                                      String nomAutrePartie, String prenomAutrePartie,
                                      String roleAutrePartie, LocalDateTime dateEntretien, String sujetStage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(destinataire);
        message.setSubject("ASM - Convocation à un entretien");
        message.setText(
                "Bonjour " + prenomDestinataire + " " + nomDestinataire + ",\n\n"
                        + "Un entretien a été planifié dans le cadre du stage \"" + sujetStage + "\".\n\n"
                        + "📅 Date et heure : " + dateEntretien.format(FORMATTER) + "\n"
                        + "👤 Avec : " + prenomAutrePartie + " " + nomAutrePartie
                        + " (" + roleAutrePartie + ")\n\n"
                        + "Merci de bien vouloir vous connecter à la plateforme pour plus de détails.\n\n"
                        + "Lien de la plateforme : http://localhost:8085\n\n"
                        + "Cordialement,\nL'équipe ASM"
        );
        mailSender.send(message);
    }

    // ===== ENCADRANT → RH =====

    public void envoyerEmailValidationEncadrantAuxRH(String nomStagiaire, String prenomStagiaire,
                                                     String nomEncadrant, String prenomEncadrant,
                                                     String sujetStage, String commentaire) {
        envoyerEmailDecisionEncadrantAuxRH(
                nomStagiaire, prenomStagiaire, nomEncadrant, prenomEncadrant,
                sujetStage, commentaire, true
        );
    }

    public void envoyerEmailRefusEncadrantAuxRH(String nomStagiaire, String prenomStagiaire,
                                                String nomEncadrant, String prenomEncadrant,
                                                String sujetStage, String commentaire) {
        envoyerEmailDecisionEncadrantAuxRH(
                nomStagiaire, prenomStagiaire, nomEncadrant, prenomEncadrant,
                sujetStage, commentaire, false
        );
    }

    private void envoyerEmailDecisionEncadrantAuxRH(String nomStagiaire, String prenomStagiaire,
                                                    String nomEncadrant, String prenomEncadrant,
                                                    String sujetStage, String commentaire,
                                                    boolean validation) {
        List<Utilisateur> rhList = utilisateurRepository.findByType(
                com.asm.gestion_stagiaires.models.RH.class
        );

        if (rhList.isEmpty()) {
            System.err.println("Aucun RH trouvé pour l'envoi d'email");
            return;
        }

        String decision = validation ? "VALIDÉ" : "REFUSÉ";
        String sujet = (validation ? "Validation" : "Refus")
                + " entretien : " + prenomStagiaire + " " + nomStagiaire;
        String suite = validation
                ? "Vous pouvez maintenant procéder à l'acceptation définitive de la candidature sur la plateforme."
                : "Veuillez consulter la plateforme pour finaliser le dossier.";

        for (Utilisateur rh : rhList) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(rh.getEmail());
            message.setSubject("ASM - " + sujet);
            message.setText(
                    "Bonjour " + rh.getPrenom() + " " + rh.getNom() + ",\n\n"
                            + "L'encadrant " + prenomEncadrant + " " + nomEncadrant
                            + " a " + decision + " le candidat suivant après son entretien :\n\n"
                            + "👤 Candidat : " + prenomStagiaire + " " + nomStagiaire + "\n"
                            + "📋 Stage : " + sujetStage + "\n"
                            + (commentaire != null && !commentaire.isBlank()
                            ? "💬 Commentaire de l'encadrant : " + commentaire + "\n\n"
                            : "\n")
                            + suite + "\n\n"
                            + "Lien de la plateforme : http://localhost:8085\n\n"
                            + "Cordialement,\nPlateforme ASM"
            );
            try {
                mailSender.send(message);
            } catch (Exception e) {
                System.err.println("Erreur envoi email au RH " + rh.getEmail() + " : " + e.getMessage());
            }
        }
    }

    // ===== Compatibilité ancienne =====

    public void envoyerIdentifiants(String destinataire, String nom, String prenom,
                                    String email, String motDePasse, String role) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(destinataire);
        message.setSubject("ASM - Vos identifiants d'accès à la plateforme");
        message.setText(
                "Bonjour " + prenom + " " + nom + ",\n\n"
                        + "Votre compte " + role + " a été créé.\n\n"
                        + "Email : " + email + "\n"
                        + "Mot de passe : " + motDePasse + "\n\n"
                        + "Cordialement,\nL'équipe ASM"
        );
        mailSender.send(message);
    }
}