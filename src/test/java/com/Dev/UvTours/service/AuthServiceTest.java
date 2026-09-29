package com.Dev.UvTours.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.Dev.UvTours.dto.AuthResponse;
import com.Dev.UvTours.dto.LoginRequest;
import com.Dev.UvTours.dto.RegistroRequest;
import com.Dev.UvTours.dto.UsuarioRequest;
import com.Dev.UvTours.entity.Cliente;
import com.Dev.UvTours.entity.Rol;
import com.Dev.UvTours.entity.Usuario;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.ClienteRepository;
import com.Dev.UvTours.repository.UsuarioRepository;
import com.Dev.UvTours.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegistroRequest registroRequest;

    @BeforeEach
    void setUp() {
        registroRequest = new RegistroRequest();
        registroRequest.setEmail("Juan@Email.com");
        registroRequest.setPassword("secreta123");
        registroRequest.setNombre("Juan Pérez");
    }

    @Test
    void testRegistrar_UsuarioNuevo() {
        // Given
        when(usuarioRepository.existsByEmail("juan@email.com")).thenReturn(false);
        when(passwordEncoder.encode("secreta123")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("token-123");

        // When
        AuthResponse response = authService.registrar(registroRequest);

        // Then
        assertEquals("juan@email.com", response.getEmail());
        assertEquals(Rol.CLIENTE.name(), response.getRol());
        assertEquals("Juan Pérez", response.getNombre());
        assertEquals("token-123", response.getToken());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("juan@email.com", captor.getValue().getEmail());
        assertEquals("hash", captor.getValue().getPasswordHash());
        assertEquals(Rol.CLIENTE, captor.getValue().getRol());
    }

    @Test
    void testRegistrar_EmailDuplicado() {
        // Given
        when(usuarioRepository.existsByEmail("juan@email.com")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> authService.registrar(registroRequest));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void testRegistrar_VinculaClientePorDni() {
        // Given
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setDni("12345678A");
        registroRequest.setDni("12345678A");

        when(usuarioRepository.existsByEmail("juan@email.com")).thenReturn(false);
        when(passwordEncoder.encode("secreta123")).thenReturn("hash");
        when(clienteRepository.findByDni("12345678A")).thenReturn(Optional.of(cliente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("token-123");

        // When
        authService.registrar(registroRequest);

        // Then
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals(cliente, captor.getValue().getCliente());
    }

    @Test
    void testLogin_CredencialesValidas() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("juan@email.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(usuarioRepository.findByEmail("juan@email.com"))
                .thenReturn(Optional.of(usuario("juan@email.com", Rol.CLIENTE)));
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("token-123");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("juan@email.com");
        loginRequest.setPassword("secreta123");

        // When
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertEquals("juan@email.com", response.getEmail());
        assertEquals("token-123", response.getToken());
    }

    @Test
    void testLogin_CredencialesInvalidas() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("juan@email.com");
        loginRequest.setPassword("mala");

        // When & Then
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testCrearUsuario_Admin() {
        // Given
        when(usuarioRepository.existsByEmail("gerente@email.com")).thenReturn(false);
        when(passwordEncoder.encode("secreta123")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("token-123");

        UsuarioRequest request = new UsuarioRequest();
        request.setEmail("Gerente@Email.com");
        request.setPassword("secreta123");
        request.setNombre("Gerente");
        request.setRol(Rol.EMPLEADO);

        // When
        AuthResponse response = authService.crearUsuario(request);

        // Then
        assertEquals("gerente@email.com", response.getEmail());
        assertEquals(Rol.EMPLEADO.name(), response.getRol());
    }

    @Test
    void testObtenerActual() {
        // Given
        when(usuarioRepository.findByEmail("juan@email.com"))
                .thenReturn(Optional.of(usuario("juan@email.com", Rol.CLIENTE)));
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("token-123");

        // When
        AuthResponse response = authService.obtenerActual("juan@email.com");

        // Then
        assertEquals("juan@email.com", response.getEmail());
        assertEquals(Rol.CLIENTE.name(), response.getRol());
    }

    @Test
    void testObtenerActual_NoEncontrado() {
        // Given
        when(usuarioRepository.findByEmail("nadie@email.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> authService.obtenerActual("nadie@email.com"));
    }

    private Usuario usuario(String email, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(email);
        usuario.setNombre("Juan Pérez");
        usuario.setPasswordHash("hash");
        usuario.setRol(rol);
        return usuario;
    }
}