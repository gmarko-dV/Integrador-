package com.integrador.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.allowed-domain}")
    private String allowedDomain;
    
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
                .requestMatchers("/api/anuncios").permitAll() // Permitir ver todos los anuncios sin autenticación
                .requestMatchers("/api/anuncios/{id}").permitAll() // Permitir ver un anuncio específico sin autenticación
                .requestMatchers("/api/chat/**").permitAll() // Permitir acceso al chat de IA sin autenticación
                .requestMatchers("/login/oauth2/code/**").permitAll()
                .requestMatchers("/uploads/**").permitAll() // Permitir acceso a imágenes subidas
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/oauth2/authorization/auth0")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(domainRestrictingOAuth2UserService())
                )
                .defaultSuccessUrl("/api/auth/success", true)
                .failureUrl("/api/auth/failure")
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

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> domainRestrictingOAuth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return userRequest -> {
            OAuth2User user = delegate.loadUser(userRequest);
            Object emailObj = user.getAttributes().get("email");
            String email = emailObj != null ? emailObj.toString() : null;
            
            System.out.println("=== VALIDACIÓN DE DOMINIO ===");
            System.out.println("Email recibido: " + email);
            System.out.println("Dominio permitido: " + allowedDomain);
            System.out.println("Email en minúsculas: " + (email != null ? email.toLowerCase() : "null"));
            System.out.println("Termina con @" + allowedDomain.toLowerCase() + ": " + 
                (email != null && email.toLowerCase().endsWith("@" + allowedDomain.toLowerCase())));
            
            if (email == null || !email.toLowerCase().endsWith("@" + allowedDomain.toLowerCase())) {
                System.out.println("ERROR: Email no permitido");
                throw new OAuth2AuthenticationException(new OAuth2Error("access_denied"),
                        "Email no pertenece al dominio institucional permitido");
            }
            
            System.out.println("Email validado correctamente");
            return user;
        };
    }
}
