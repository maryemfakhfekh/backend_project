package com.asm.gestion_stagiaires.services;

import com.asm.gestion_stagiaires.models.*;
import com.asm.gestion_stagiaires.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;

@Service
public class IAService {

    @Autowired private AnalyseIARepository analyseIARepository;
    @Autowired private QuestionEntretienRepository questionEntretienRepository;
    @Autowired private CandidatureRepository candidatureRepository;

    private final RestTemplate restTemplate = createRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String FASTAPI_URL = "http://localhost:8000";

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);   // 10 secondes
        factory.setReadTimeout(120000);     // 120 secondes pour LLaMA
        return new RestTemplate(factory);
    }

    private String getCvDir() {
        return System.getProperty("user.dir") + "/uploads/cvs/";
    }

    public AnalyseIA analyserCV(Long candidatureId) {

        // 1) Récupérer la candidature
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        SujetStage sujet = candidature.getSujet();
        String cvPath = getCvDir() + candidature.getCvPath();
        File cvFile = new File(cvPath);

        // Log pour debug
        System.out.println("Répertoire travail : " + System.getProperty("user.dir"));
        System.out.println("Chemin CV : " + cvPath);
        System.out.println("Fichier existe : " + cvFile.exists());

        if (!cvFile.exists()) {
            throw new RuntimeException("CV introuvable : " + cvPath);
        }

        // 2) Construire le sujet JSON pour FastAPI
        Map<String, Object> sujetMap = new HashMap<>();
        sujetMap.put("title", sujet.getTitre());
        sujetMap.put("description", sujet.getDescription() != null ?
                sujet.getDescription() : "");
        sujetMap.put("required_skills", sujet.getCompetencesCibles() != null ?
                sujet.getCompetencesCibles() : new ArrayList<>());
        sujetMap.put("filiere", sujet.getFiliereCible() != null ?
                sujet.getFiliereCible().getNom() : null);
        sujetMap.put("cycle", sujet.getCycleCible() != null ?
                sujet.getCycleCible().getNom() : null);

        String sujetJson;
        try {
            sujetJson = objectMapper.writeValueAsString(sujetMap);
        } catch (Exception e) {
            throw new RuntimeException("Erreur JSON sujet : " + e.getMessage());
        }

        // 3) Appeler FastAPI score
        Map<String, Object> scoreResponse = appellerFastAPIScore(cvFile, sujetJson);

        // 4) Appeler FastAPI questions
        Map<String, Object> questionsResponse = appellerFastAPIQuestions(cvFile, sujetJson);

        // 5) Sauvegarder AnalyseIA
        Optional<AnalyseIA> existingAnalyse = analyseIARepository
                .findByCandidatureId(candidatureId);
        AnalyseIA analyse = existingAnalyse.orElse(new AnalyseIA());

        analyse.setCandidature(candidature);
        analyse.setScoreGlobal(((Number) scoreResponse.get("final_score")).doubleValue());
        analyse.setRecommendation((String) scoreResponse.get("recommendation"));
        analyse.setRecommendationLabel((String) scoreResponse.get("recommendation_label"));
        analyse.setJustification((String) scoreResponse.get("justification"));
        analyse.setSemanticSimilarity(
                ((Number) scoreResponse.get("semantic_similarity")).doubleValue());
        analyse.setAuditId((String) scoreResponse.get("audit_id"));

        // Piliers
        Map<String, Object> pillars = (Map<String, Object>) scoreResponse.get("pillars");
        if (pillars != null) {
            Map<String, Object> skills = (Map<String, Object>) pillars.get("skills");
            Map<String, Object> formation = (Map<String, Object>) pillars.get("formation");
            Map<String, Object> experience = (Map<String, Object>) pillars.get("experience");

            if (skills != null) {
                analyse.setScoreCompetences(
                        ((Number) skills.get("score")).doubleValue());
                analyse.setCompetencesMatchees((List<String>) skills.get("matched"));
                analyse.setCompetencesManquantes((List<String>) skills.get("missing"));
            }
            if (formation != null) {
                analyse.setScoreFormation(
                        ((Number) formation.get("score")).doubleValue());
            }
            if (experience != null) {
                analyse.setScoreExperience(
                        ((Number) experience.get("score")).doubleValue());
            }
        }

        // Education lines depuis cv_info
        Map<String, Object> cvInfo = (Map<String, Object>) scoreResponse.get("cv_info");
        if (cvInfo != null) {
            analyse.setEducationLines((List<String>) cvInfo.get("education_lines"));
        }

        analyseIARepository.save(analyse);

        // 6) Supprimer anciennes questions et sauvegarder nouvelles
        List<QuestionEntretien> oldQuestions =
                questionEntretienRepository.findByCandidatureId(candidatureId);
        if (!oldQuestions.isEmpty()) {
            questionEntretienRepository.deleteAll(oldQuestions);
        }

        List<Map<String, String>> questions =
                (List<Map<String, String>>) questionsResponse.get("questions");
        if (questions != null) {
            for (Map<String, String> q : questions) {
                QuestionEntretien question = new QuestionEntretien();
                question.setCandidature(candidature);
                question.setCategory(q.get("category"));
                question.setQuestion(q.get("question"));
                question.setSource((String) questionsResponse.get("source"));
                questionEntretienRepository.save(question);
            }
        }

        // 7) Mettre à jour le score dans Candidature
        candidature.setScoreMatchingIA(analyse.getScoreGlobal());
        candidatureRepository.save(candidature);

        return analyse;
    }

    private Map<String, Object> appellerFastAPIScore(File cvFile, String sujetJson) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(cvFile));
            body.add("subject", sujetJson);

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    FASTAPI_URL + "/api/ia/score-compatibilite", request, Map.class);

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erreur appel FastAPI score : " + e.getMessage());
        }
    }

    private Map<String, Object> appellerFastAPIQuestions(File cvFile, String sujetJson) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(cvFile));
            body.add("subject", sujetJson);

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    FASTAPI_URL + "/api/entretien/questions", request, Map.class);

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erreur appel FastAPI questions : " + e.getMessage());
        }
    }
}