package com.example.demoLTI13.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers
                        .addHeaderWriter((request, response) -> {
                            response.setHeader("X-Frame-Options", "ALLOWALL");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/lti/login", "/lti/launch", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Arrays.asList("http://localhost:8100")); // Cho phép Moodle
                    config.setAllowedMethods(Arrays.asList("GET", "POST"));
                    config.setAllowedHeaders(Arrays.asList("*"));
                    return config;
                }));

        return http.build();
    }
}