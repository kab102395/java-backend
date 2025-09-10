package com.example.gamebackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // CORS + CSRF
    http
      .cors(Customizer.withDefaults())
      // Option 1: disable CSRF entirely for dev:
      .csrf(csrf -> csrf.disable());
      // Option 2 (alternative): ignore only specific paths
      // .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/actuator/**", "/health", "/ws/**"));

    // Authorization rules
    http
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/**", "/actuator/**", "/health", "/ws/**").permitAll()
        .anyRequest().permitAll());

    // Stateless (good for token/JWT flows; safe for dev)
    http
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    // Dev-friendly: allow anything. Tighten for prod.
    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(false);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
