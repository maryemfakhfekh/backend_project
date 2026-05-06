package com.asm.gestion_stagiaires.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ✅ Encadrants — doit être AVANT le permitAll de /api/auth/**
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/auth/encadrants")
                        .hasAuthority("ROLE_RH")

                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/references/**").permitAll()
                        .requestMatchers("/api/cv/**").permitAll()
                        .requestMatchers("/api/demandes-acces/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ Stats RH — accessible au RH et Admin
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/admin/stats")
                        .hasAnyAuthority("ROLE_RH", "ROLE_ADMIN")

                        // ✅ Admin — reste protégé
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // ✅ Stages — endpoints spécifiques stagiaire (AVANT la règle générique)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/stages/has-dossier")
                        .hasAnyAuthority("ROLE_RH", "ROLE_ADMIN", "ROLE_STAGIAIRE")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/stages/mon-dossier")
                        .hasAnyAuthority("ROLE_RH", "ROLE_ADMIN", "ROLE_STAGIAIRE")

                        // ✅ Stages — liste générale RH et Admin
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/stages")
                        .hasAnyAuthority("ROLE_RH", "ROLE_ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/stages/**")
                        .hasAnyAuthority("ROLE_RH", "ROLE_ADMIN", "ROLE_ENCADRANT")

                        // ✅ Encadrants — endpoints spécifiques encadrant
                        .requestMatchers("/api/encadrants/**").hasAuthority("ROLE_ENCADRANT")

                        // ✅ Candidatures
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/candidatures")
                        .hasAnyAuthority("ROLE_RH", "ROLE_ADMIN", "ROLE_STAGIAIRE", "ROLE_ENCADRANT")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/candidatures/**")
                        .hasAnyAuthority("ROLE_RH", "ROLE_ADMIN", "ROLE_STAGIAIRE", "ROLE_ENCADRANT")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/candidatures/**")
                        .hasAnyAuthority("ROLE_RH", "ROLE_ADMIN", "ROLE_ENCADRANT")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/candidatures/**")
                        .hasAnyAuthority("ROLE_RH", "ROLE_ADMIN", "ROLE_STAGIAIRE")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/candidatures/**")
                        .hasAnyAuthority("ROLE_RH", "ROLE_ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/candidatures/encadrants")
                        .hasAuthority("ROLE_RH")
                        // ✅ Evaluations
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/evaluations")
                        .hasAnyAuthority("ROLE_RH", "ROLE_ENCADRANT", "ROLE_STAGIAIRE", "ROLE_ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/evaluations/**")
                        .hasAnyAuthority("ROLE_RH", "ROLE_ENCADRANT", "ROLE_STAGIAIRE", "ROLE_ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/evaluations/**")
                        .hasAuthority("ROLE_ENCADRANT")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}