package com.integrador.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    @Value("${supabase.jwt.secret:}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        // Configuración para Supabase
        String supabaseUrl = "https://kkjjgvqqzxothhojvzss.supabase.co";
        
        System.out.println("🔧 Configurando JWT Decoder para Supabase:");
        
        // Si tenemos el JWT Secret, usar HS256 (algoritmo simétrico)
        if (jwtSecret != null && !jwtSecret.isEmpty()) {
            System.out.println("  Usando HS256 con JWT Secret");
            try {
                // Crear la clave secreta para HS256
                SecretKey secretKey = new SecretKeySpec(
                    jwtSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
                );
                
                NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey).build();
                System.out.println("✅ JWT Decoder creado exitosamente con HS256");
            
            // Validador personalizado que acepta cualquier issuer de Supabase
            // (puede ser la URL base o /auth/v1 dependiendo de la versión)
            OAuth2TokenValidator<Jwt> issuerValidator = new OAuth2TokenValidator<Jwt>() {
                @Override
                public org.springframework.security.oauth2.core.OAuth2TokenValidatorResult validate(Jwt jwt) {
                    try {
                        String issuer = jwt.getIssuer().toString();
                        System.out.println("🔍 Validando JWT - Issuer del token: " + issuer);
                        System.out.println("🔍 Subject (sub): " + jwt.getSubject());
                        System.out.println("🔍 Audience: " + jwt.getAudience());
                        System.out.println("🔍 Expires at: " + jwt.getExpiresAt());
                        
                        // Aceptar si el issuer contiene la URL de Supabase
                        if (issuer != null && issuer.contains(supabaseUrl)) {
                            System.out.println("✅ Issuer válido: " + issuer);
                            return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
                        } else {
                            System.out.println("❌ Issuer inválido: " + issuer + " (esperado: contiene " + supabaseUrl + ")");
                            return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                                new org.springframework.security.oauth2.core.OAuth2Error(
                                    "invalid_token", 
                                    "El issuer del token no es válido: " + issuer, 
                                    null
                                )
                            );
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Error al validar JWT: " + e.getMessage());
                        e.printStackTrace();
                        return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                            new org.springframework.security.oauth2.core.OAuth2Error(
                                "invalid_token", 
                                "Error al validar el token: " + e.getMessage(), 
                                null
                            )
                        );
                    }
                }
            };
            
            // Combinar validadores: timestamp + issuer personalizado
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                issuerValidator
            ));
            
            return decoder;
            } catch (Exception e) {
                System.out.println("❌ Error al crear JWT Decoder con HS256: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("No se pudo crear el JWT Decoder con HS256", e);
            }
        } else {
            // Si no hay JWT Secret, intentar usar JWK Set URI (RS256)
            String jwkSetUri = supabaseUrl + "/auth/v1/.well-known/jwks.json";
            System.out.println("  Intentando usar JWK Set URI (RS256): " + jwkSetUri);
            
            try {
                NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
                System.out.println("✅ JWT Decoder creado exitosamente con JWK Set URI");
                
                // Validador personalizado
                OAuth2TokenValidator<Jwt> issuerValidator = new OAuth2TokenValidator<Jwt>() {
                    @Override
                    public org.springframework.security.oauth2.core.OAuth2TokenValidatorResult validate(Jwt jwt) {
                        try {
                            String issuer = jwt.getIssuer().toString();
                            System.out.println("🔍 Validando JWT - Issuer del token: " + issuer);
                            
                            if (issuer != null && issuer.contains(supabaseUrl)) {
                                System.out.println("✅ Issuer válido: " + issuer);
                                return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
                            } else {
                                System.out.println("❌ Issuer inválido: " + issuer);
                                return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                                    new org.springframework.security.oauth2.core.OAuth2Error(
                                        "invalid_token", 
                                        "El issuer del token no es válido: " + issuer, 
                                        null
                                    )
                                );
                            }
                        } catch (Exception e) {
                            System.out.println("❌ Error al validar JWT: " + e.getMessage());
                            return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                                new org.springframework.security.oauth2.core.OAuth2Error(
                                    "invalid_token", 
                                    "Error al validar el token: " + e.getMessage(), 
                                    null
                                )
                            );
                        }
                    }
                };
                
                decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                    new JwtTimestampValidator(),
                    issuerValidator
                ));
                
                return decoder;
            } catch (Exception e) {
                System.out.println("❌ Error al crear JWT Decoder con JWK Set URI: " + e.getMessage());
                System.out.println("⚠️  NOTA: Necesitas configurar 'supabase.jwt.secret' en application.properties");
                System.out.println("   Obtén el JWT Secret de: Supabase Dashboard > Settings > API > JWT Secret");
                e.printStackTrace();
                throw new RuntimeException("No se pudo crear el JWT Decoder. Configura 'supabase.jwt.secret' en application.properties", e);
            }
        }
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("permissions");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}
