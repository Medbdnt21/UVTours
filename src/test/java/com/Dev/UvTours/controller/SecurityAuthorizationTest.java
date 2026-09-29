package com.Dev.UvTours.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.Dev.UvTours.dto.AuthResponse;
import com.Dev.UvTours.dto.CompraDTO;
import com.Dev.UvTours.repository.ClienteRepository;
import com.Dev.UvTours.repository.UsuarioRepository;
import com.Dev.UvTours.security.JwtService;
import com.Dev.UvTours.security.OAuth2LoginSuccessHandler;
import com.Dev.UvTours.security.SecurityBeansConfig;
import com.Dev.UvTours.security.SecurityConfig;
import com.Dev.UvTours.security.UsuarioDetailsService;
import com.Dev.UvTours.service.AuthService;
import com.Dev.UvTours.service.CompraService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = { CompraController.class, AuthController.class })
@Import({ SecurityConfig.class, SecurityBeansConfig.class, JwtService.class, UsuarioDetailsService.class,
        OAuth2LoginSuccessHandler.class })
@TestPropertySource(properties = {
        "jwt.secret=TestSecretKeyParaJWT_HS256_0123456789abcdef",
        "jwt.expiration-ms=60000",
        "app.oauth2.enabled=false",
        "app.oauth2.redirect-uri=" })
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompraService compraService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @Test
    void testSinTokenDevuelve401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/compras"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminListaCompras() throws Exception {
        // Given
        when(compraService.listarCompras()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/compras"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void testEmpleadoListaCompras() throws Exception {
        // Given
        when(compraService.listarCompras()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/compras"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void testClienteNoListaComprasDeTodos() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/compras"))
                .andExpect(status().isForbidden());

        verify(compraService, never()).listarCompras();
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void testClienteVeSusCompras() throws Exception {
        // Given
        when(compraService.misCompras("user")).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/compras/mias"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void testClienteNoPuedeComprarEnMostrador() throws Exception {
        // Given
        CompraDTO compraDTO = new CompraDTO();
        compraDTO.setClienteId(1L);
        compraDTO.setViajeId(1L);

        // When & Then
        mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraDTO)))
                .andExpect(status().isForbidden());

        verify(compraService, never()).realizarCompra(any(CompraDTO.class));
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void testEmpleadoSiPuedeCrearCompra() throws Exception {
        // Given
        CompraDTO compraDTO = new CompraDTO();
        compraDTO.setClienteId(1L);
        compraDTO.setViajeId(1L);

        when(compraService.realizarCompra(any(CompraDTO.class))).thenReturn(compraDTO);

        // When & Then
        mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testRegistroYLoginPermanecenPublicos() throws Exception {
        // Given
        AuthResponse response = new AuthResponse("token-1", "nuevo@email.com", "CLIENTE", "Nuevo");
        when(authService.registrar(any())).thenReturn(response);

        String body = "{\"email\":\"nuevo@email.com\",\"password\":\"secreta123\",\"nombre\":\"Nuevo\"}";

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("nuevo@email.com")));
    }

    @Test
    @WithMockUser
    void testMeDevuelveUsuarioActual() throws Exception {
        // Given
        AuthResponse response = new AuthResponse("token-1", "user", "CLIENTE", "User");
        when(authService.obtenerActual("user")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("user")));
    }
}