package com.Dev.UvTours.security;

import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import com.Dev.UvTours.entity.Rol;
import com.Dev.UvTours.entity.Usuario;

class JwtServiceTest {

    private static final String SECRET = "TestSecretKeyParaJWT_HS256_0123456789abcdef";

    @Test
    void testGenerarYValidarToken() {
        // Given
        JwtService jwtService = new JwtService(SECRET, 60000L);
        Usuario usuario = new Usuario();
        usuario.setEmail("juan@email.com");
        usuario.setNombre("Juan");
        usuario.setRol(Rol.CLIENTE);

        // When
        String token = jwtService.generarToken(usuario);
        Jwt jwt = jwtService.validarToken(token);

        // Then
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("juan@email.com", jwt.getSubject());
        assertEquals("CLIENTE", jwt.getClaimAsString("rol"));
        assertEquals("Juan", jwt.getClaimAsString("nombre"));
    }

    @Test
    void testRolEnToken() {
        // Given
        JwtService jwtService = new JwtService(SECRET, 60000L);
        Usuario usuario = new Usuario();
        usuario.setEmail("admin@email.com");
        usuario.setNombre("Admin");
        usuario.setRol(Rol.ADMIN);

        // When
        Jwt jwt = jwtService.validarToken(jwtService.generarToken(usuario));

        // Then
        assertEquals("ADMIN", jwt.getClaimAsString("rol"));
    }

    @Test
    void testTokenExpirado() {
        // Given: token ya caducado (issuedAt y expiresAt en el pasado, en orden válido)
        JwtService jwtService = new JwtService(SECRET, 60000L);
        SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        JwtClaimsSet claimsExpirados = JwtClaimsSet.builder()
                .issuer("UvTours")
                .issuedAt(Instant.now().minusSeconds(600))
                .expiresAt(Instant.now().minusSeconds(300))
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claimsExpirados)).getTokenValue();

        // When & Then
        assertThrows(JwtException.class, () -> jwtService.validarToken(token));
    }
}