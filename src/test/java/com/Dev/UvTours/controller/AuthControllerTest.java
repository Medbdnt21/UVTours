package com.Dev.UvTours.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.Dev.UvTours.dto.AuthResponse;
import com.Dev.UvTours.dto.LoginRequest;
import com.Dev.UvTours.dto.RegistroRequest;
import com.Dev.UvTours.dto.UsuarioRequest;
import com.Dev.UvTours.service.AuthService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void testRegistrar() throws Exception {
        // Given
        AuthResponse response = new AuthResponse("token-123", "juan@email.com", "CLIENTE", "Juan Pérez");
        when(authService.registrar(any(RegistroRequest.class))).thenReturn(response);

        RegistroRequest request = new RegistroRequest();
        request.setEmail("juan@email.com");
        request.setPassword("secreta123");
        request.setNombre("Juan Pérez");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("token-123")))
                .andExpect(jsonPath("$.email", is("juan@email.com")))
                .andExpect(jsonPath("$.rol", is("CLIENTE")));

        verify(authService, times(1)).registrar(any(RegistroRequest.class));
    }

    @Test
    void testRegistrar_ValidacionEmailInvalido() throws Exception {
        // Given
        RegistroRequest request = new RegistroRequest();
        request.setEmail("no-es-email");
        request.setPassword("secreta123");
        request.setNombre("Juan Pérez");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registrar(any(RegistroRequest.class));
    }

    @Test
    void testRegistrar_ValidacionPasswordCorta() throws Exception {
        // Given
        RegistroRequest request = new RegistroRequest();
        request.setEmail("juan@email.com");
        request.setPassword("123");
        request.setNombre("Juan Pérez");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin() throws Exception {
        // Given
        AuthResponse response = new AuthResponse("token-123", "juan@email.com", "CLIENTE", "Juan Pérez");
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        LoginRequest request = new LoginRequest();
        request.setEmail("juan@email.com");
        request.setPassword("secreta123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("token-123")));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testCrearUsuario() throws Exception {
        // Given
        AuthResponse response = new AuthResponse("token-456", "gerente@email.com", "EMPLEADO", "Gerente");
        when(authService.crearUsuario(any(UsuarioRequest.class))).thenReturn(response);

        UsuarioRequest request = new UsuarioRequest();
        request.setEmail("gerente@email.com");
        request.setPassword("secreta123");
        request.setNombre("Gerente");
        request.setRol(com.Dev.UvTours.entity.Rol.EMPLEADO);

        // When & Then
        mockMvc.perform(post("/api/auth/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol", is("EMPLEADO")));

        verify(authService, times(1)).crearUsuario(any(UsuarioRequest.class));
    }
}