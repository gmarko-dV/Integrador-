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
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    @Value("${supabase.jwt.secret:}")
    private String jwtSecret;
    
    @Value("${supabase.anon.key:}")
    private String anonKey;

    @Bean
    public JwtDecoder jwtDecoder() {
        // Configuración para Supabase
        String supabaseUrl = "https://kkjjgvqqzxothhojvzss.supabase.co";
        
        System.out.println("🔧 Configurando JWT Decoder para Supabase:");
        System.out.println("  JWT Secret configurado: " + (jwtSecret != null && !jwtSecret.isEmpty() ? "SÍ" : "NO"));
        System.out.println("  Anon Key configurado: " + (anonKey != null && !anonKey.isEmpty() ? "SÍ" : "NO"));
        System.out.println("  ⚠️ IMPORTANTE: Usando decoder que intenta ambos algoritmos (HS256 y RS256)");
        
        // SIEMPRE usar el decoder de fallback que intenta ambos algoritmos
        // porque Supabase puede usar HS256 o RS256 dependiendo de la configuración
        return createFallbackDecoder(supabaseUrl);
    }
    
    /**
     * Decoder personalizado que intenta múltiples métodos de decodificación
     */
    private JwtDecoder createFallbackDecoder(String supabaseUrl) {
        return new JwtDecoder() {
            private JwtDecoder hs256Decoder = null;
            private JwtDecoder rs256Decoder = null;
            
            {
                // Intentar crear decoder HS256 con JWT Secret (si está configurado)
                if (jwtSecret != null && !jwtSecret.isEmpty()) {
                    try {
                        SecretKey secretKey = new SecretKeySpec(
                            jwtSecret.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"
                        );
                        hs256Decoder = NimbusJwtDecoder.withSecretKey(secretKey).build();
                        System.out.println("✅ Decoder HS256 con JWT Secret creado");
                    } catch (Exception e) {
                        System.out.println("⚠️ No se pudo crear decoder HS256 con JWT Secret: " + e.getMessage());
                    }
                }
                
                // Si no hay JWT Secret, intentar con anon key (aunque probablemente no funcione)
                if (hs256Decoder == null && anonKey != null && !anonKey.isEmpty()) {
                    try {
                        SecretKey secretKey = new SecretKeySpec(
                            anonKey.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"
                        );
                        hs256Decoder = NimbusJwtDecoder.withSecretKey(secretKey).build();
                        System.out.println("⚠️ Decoder HS256 con anon key creado (puede no funcionar - necesitas JWT Secret)");
                    } catch (Exception e) {
                        System.out.println("⚠️ No se pudo crear decoder HS256 con anon key: " + e.getMessage());
                    }
                }
                
                // Intentar crear decoder con JWK Set URI (puede ser RS256, ES256, etc.)
                try {
                    String jwkSetUri = supabaseUrl + "/auth/v1/.well-known/jwks.json";
                    rs256Decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
                    System.out.println("✅ Decoder con JWK Set URI creado (soporta RS256, ES256, etc.)");
                } catch (Exception e) {
                    System.out.println("⚠️ No se pudo crear decoder con JWK Set URI: " + e.getMessage());
                }
            }
            
            @Override
            public org.springframework.security.oauth2.jwt.Jwt decode(String token) throws JwtException {
                // Primero, detectar el algoritmo del token decodificando el header
                String detectedAlgorithm = null;
                try {
                    String[] parts = token.split("\\.");
                    if (parts.length >= 1) {
                        String headerJson = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
                        System.out.println("🔍 JWT Header: " + headerJson);
                        // Extraer el algoritmo del header
                        if (headerJson.contains("\"alg\"")) {
                            if (headerJson.contains("HS256")) {
                                detectedAlgorithm = "HS256";
                            } else if (headerJson.contains("RS256")) {
                                detectedAlgorithm = "RS256";
                            } else if (headerJson.contains("ES256")) {
                                detectedAlgorithm = "ES256";
                            }
                        }
                        System.out.println("🔍 Algoritmo detectado en el token: " + (detectedAlgorithm != null ? detectedAlgorithm : "desconocido"));
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ No se pudo decodificar el header del JWT: " + e.getMessage());
                }
                
                // Si detectamos HS256, intentar primero con HS256
                if ("HS256".equals(detectedAlgorithm) && hs256Decoder != null) {
                    try {
                        System.out.println("🔍 Token es HS256, intentando decodificar con HS256...");
                        Jwt jwt = hs256Decoder.decode(token);
                        System.out.println("✅ Token decodificado exitosamente con HS256");
                        System.out.println("   Subject (sub): " + jwt.getSubject());
                        System.out.println("   Issuer: " + jwt.getIssuer());
                        return jwt;
                    } catch (JwtException e) {
                        System.out.println("❌ Falló decodificación HS256: " + e.getMessage());
                        System.out.println("   ⚠️ Esto significa que el JWT Secret no es correcto.");
                        System.out.println("   Necesitas obtener el JWT Secret real de: Supabase Dashboard > Settings > API > JWT Secret");
                    }
                }
                
                // Si detectamos RS256 o ES256, intentar con el decoder de JWK Set URI
                if (("RS256".equals(detectedAlgorithm) || "ES256".equals(detectedAlgorithm)) && rs256Decoder != null) {
                    try {
                        System.out.println("🔍 Token es " + detectedAlgorithm + ", intentando decodificar con JWK Set URI...");
                        Jwt jwt = rs256Decoder.decode(token);
                        System.out.println("✅ Token decodificado exitosamente con " + detectedAlgorithm);
                        System.out.println("   Subject (sub): " + jwt.getSubject());
                        System.out.println("   Issuer: " + jwt.getIssuer());
                        return jwt;
                    } catch (JwtException e) {
                        System.out.println("❌ Falló decodificación " + detectedAlgorithm + ": " + e.getMessage());
                    }
                }
                
                // Si no detectamos el algoritmo o falló el método preferido, intentar ambos
                if (hs256Decoder != null) {
                    try {
                        System.out.println("🔍 Intentando decodificar con HS256 (fallback)...");
                        Jwt jwt = hs256Decoder.decode(token);
                        System.out.println("✅ Token decodificado exitosamente con HS256");
                        System.out.println("   Subject (sub): " + jwt.getSubject());
                        System.out.println("   Issuer: " + jwt.getIssuer());
                        return jwt;
                    } catch (JwtException e) {
                        System.out.println("⚠️ Falló decodificación HS256: " + e.getMessage());
                    }
                }
                
                if (rs256Decoder != null) {
                    try {
                        System.out.println("🔍 Intentando decodificar con RS256 (fallback)...");
                        Jwt jwt = rs256Decoder.decode(token);
                        System.out.println("✅ Token decodificado exitosamente con RS256");
                        System.out.println("   Subject (sub): " + jwt.getSubject());
                        System.out.println("   Issuer: " + jwt.getIssuer());
                        return jwt;
                    } catch (JwtException e) {
                        System.out.println("⚠️ Falló decodificación RS256: " + e.getMessage());
                    }
                }
                
                throw new JwtException("No se pudo decodificar el token con ningún método disponible. " +
                    "El token usa " + (detectedAlgorithm != null ? detectedAlgorithm : "algoritmo desconocido") + ". " +
                    "Configura 'supabase.jwt.secret' en application.properties. " +
                    "Obtén el JWT Secret de: Supabase Dashboard > Settings > API > JWT Secret");
            }
        };
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
