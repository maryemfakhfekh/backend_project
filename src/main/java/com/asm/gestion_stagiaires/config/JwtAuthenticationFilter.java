package com.asm.gestion_stagiaires.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // ✅ Toujours ignorer OPTIONS (preflight CORS)
        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        // ✅ Routes vraiment publiques — sans token du tout
        // ⚠️ /api/references/ est retiré d'ici volontairement :
        //    le filtre doit s'exécuter pour établir le SecurityContext
        //    même si le token est absent — permitAll() gérera l'accès
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/cv/")
                || path.startsWith("/api/demandes-acces/")
                || path.startsWith("/uploads/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getServletPath();
        logger.info(">>> JWT Filter — {} {}", method, path);

        String authHeader = request.getHeader("Authorization");
        logger.info(">>> Authorization header: {}", authHeader != null ? "présent" : "ABSENT");

        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtils.getEmailFromToken(token);
                logger.info(">>> Username extrait du token: {}", username);
            } catch (Exception e) {
                logger.error(">>> Erreur extraction token: {}", e.getMessage());
            }
        }

        // ✅ Si pas de token → on laisse passer sans authentification
        //    permitAll() dans SecurityConfig prendra le relais
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.info(">>> Authorities: {}", userDetails.getAuthorities());

                if (jwtUtils.validateToken(token)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info(">>> Authentification OK pour: {}", username);
                } else {
                    logger.warn(">>> Token invalide pour: {}", username);
                }
            } catch (Exception e) {
                logger.error(">>> Erreur chargement user: {}", e.getMessage());
            }
        } else if (username == null) {
            logger.info(">>> Pas de token — accès anonyme, permitAll() gérera");
        }

        filterChain.doFilter(request, response);
    }
}