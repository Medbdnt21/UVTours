package com.Dev.UvTours.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import com.Dev.UvTours.entity.Rol;
import com.Dev.UvTours.entity.Usuario;
import com.Dev.UvTours.repository.ClienteRepository;
import com.Dev.UvTours.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testUsuarioNuevoSeRegistraYDevuelveToken() throws Exception {
        // Given
        when(usuarioRepository.findByEmail("juan@email.com")).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail("juan@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash-aleatorio");
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("jwt-token-123");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        OAuth2LoginSuccessHandler handler = new OAuth2LoginSuccessHandler(
                usuarioRepository, clienteRepository, jwtService, passwordEncoder, objectMapper);

        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // When
        handler.onAuthenticationSuccess(mock(HttpServletRequest.class), response, authToken("juan@email.com", "Juan"));

        // Then
        verify(usuarioRepository).save(argThat(u ->
                "juan@email.com".equals(u.getEmail())
                        && u.getRol() == Rol.CLIENTE
                        && "hash-aleatorio".equals(u.getPasswordHash())));
        verify(response).setStatus(HttpServletResponse.SC_OK);
        String body = stringWriter.toString();
        assertTrue(body.contains("jwt-token-123"));
        assertTrue(body.contains("juan@email.com"));
        assertTrue(body.contains("CLIENTE"));
    }

    @Test
    void testUsuarioExistenteNoSeCreaDeNuevo() throws Exception {
        // Given
        Usuario existente = new Usuario();
        existente.setEmail("juan@email.com");
        existente.setNombre("Juan");
        existente.setRol(Rol.CLIENTE);
        when(usuarioRepository.findByEmail("juan@email.com")).thenReturn(Optional.of(existente));
        when(jwtService.generarToken(existente)).thenReturn("jwt-token-123");

        OAuth2LoginSuccessHandler handler = new OAuth2LoginSuccessHandler(
                usuarioRepository, clienteRepository, jwtService, passwordEncoder, objectMapper);

        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // When
        handler.onAuthenticationSuccess(mock(HttpServletRequest.class), response, authToken("juan@email.com", "Juan"));

        // Then
        verify(usuarioRepository, never()).save(any(Usuario.class));
        assertTrue(stringWriter.toString().contains("jwt-token-123"));
    }

    @Test
    void testConRedirectUriRedirigeConToken() throws Exception {
        // Given
        Usuario existente = new Usuario();
        existente.setEmail("juan@email.com");
        existente.setNombre("Juan");
        existente.setRol(Rol.CLIENTE);
        when(usuarioRepository.findByEmail("juan@email.com")).thenReturn(Optional.of(existente));
        when(jwtService.generarToken(existente)).thenReturn("jwt-token-123");

        OAuth2LoginSuccessHandler handler = new OAuth2LoginSuccessHandler(
                usuarioRepository, clienteRepository, jwtService, passwordEncoder, objectMapper);
        ReflectionTestUtils.setField(handler, "redirectUri", "http://localhost:5173/oauth2/callback");

        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        handler.onAuthenticationSuccess(mock(HttpServletRequest.class), response, authToken("juan@email.com", "Juan"));

        // Then
        verify(response).sendRedirect("http://localhost:5173/oauth2/callback?token=jwt-token-123");
    }

    private OAuth2AuthenticationToken authToken(String email, String nombre) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", email);
        attributes.put("name", nombre);
        OAuth2User oauthUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), attributes, "email");
        return new OAuth2AuthenticationToken(oauthUser, oauthUser.getAuthorities(), "github");
    }
}