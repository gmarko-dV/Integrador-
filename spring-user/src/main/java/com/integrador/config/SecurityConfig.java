package com.integrador.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtDecoder jwtDecoder;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private com.integrador.config.JwtExceptionHandler jwtExceptionHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/plate-search/**").permitAll()
                // GET de anuncios: público (cualquiera puede ver anuncios)
                .requestMatchers(HttpMethod.GET, "/api/anuncios").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/anuncios/").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/anuncios/{id}").permitAll()
                // POST, PUT, DELETE de anuncios: requieren autenticación
                .requestMatchers(HttpMethod.POST, "/api/anuncios/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/anuncios/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/anuncios/**").authenticated()
                // Mis anuncios: requiere autenticación
                .requestMatchers("/api/anuncios/mis-anuncios").authenticated()
                .requestMatchers("/api/chat/**").permitAll() // Permitir acceso al chat de IA sin autenticación
                .requestMatchers("/api/notificaciones/**").authenticated() // Notificaciones requieren autenticación
                .requestMatchers("/login/oauth2/code/**").permitAll()
                .requestMatchers("/uploads/**").permitAll() // Permitir acceso a imágenes subidas
                .requestMatchers("/error").permitAll() // Permitir página de error
                .requestMatchers("/favicon.ico").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder))
                .authenticationEntryPoint(jwtExceptionHandler)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/api/auth/logout-success")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
