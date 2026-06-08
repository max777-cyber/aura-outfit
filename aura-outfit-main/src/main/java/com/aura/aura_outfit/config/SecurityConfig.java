package com.aura.aura_outfit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Lista de origens permitidas. Configure no application.properties:
     *   app.cors.allowed-origins=http://localhost:8080,http://localhost:5500,https://seu-dominio.com
     *
     * ❌ NUNCA use "*" com allowCredentials=true — é CSRF em bandeja.
     */
    @Value("${app.cors.allowed-origins:http://localhost:8080,http://127.0.0.1:8080,http://localhost:5500,http://127.0.0.1:5500}")
    private String allowedOriginsCsv;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // strength 12 — mais lento pra brute-force. Padrão do Spring é 10.
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OAuth2LoginSuccessHandler oauth2LoginSuccessHandler) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF desabilitado porque a autenticação é por sessão e o cookie
            // usa SameSite=Lax (configurado no application.properties).
            // A API é stateless do ponto de vista de token CSRF.
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(f -> f.deny())
                .contentTypeOptions(c -> {})
            )
            // A autorização fina é feita nos controllers via SessionUtil.
            // O Spring Security aqui só entrega o request.
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login.html")
                .successHandler(oauth2LoginSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    response.sendRedirect("/login.html?oauth2=erro");
                })
            );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origens = Arrays.stream(allowedOriginsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        // ✅ setAllowedOrigins (não patterns com "*") + credenciais = CORS seguro.
        config.setAllowedOrigins(origens);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}