package com.ticket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.annotation.PostConstruct;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().configurationSource(corsConfigurationSource()).and()
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        //tickets
                        .requestMatchers(HttpMethod.GET, "/api/tickets").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tickets/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tickets/*/comentarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tickets/usuario/*").authenticated()

                        //crear tickets y comentarios
                        .requestMatchers(HttpMethod.POST, "/api/tickets").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/tickets/*/comentarios").authenticated()

                        //actualizar tickets, solo usuarios autenticados
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/*/estado").authenticated()

                        //Solo el admin puede eliminar tickets
                        .requestMatchers(HttpMethod.DELETE, "/api/tickets/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/**").hasRole("ADMIN")

                        //caulqueir otra peticion requeire autenticacion
                        .anyRequest().authenticated()
                )
                .httpBasic()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:8080")); // Frontend Vue
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true); // Para futuros tokens/cookies
        config.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void init() {
        System.out.println(">>> Configuración de seguridad cargada");
        System.out.println(">>> Endpoints protegidos: GET /api/tickets/**");
    }
}