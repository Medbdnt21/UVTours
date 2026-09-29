package com.Dev.UvTours.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Dev.UvTours.dto.AuthResponse;
import com.Dev.UvTours.dto.LoginRequest;
import com.Dev.UvTours.dto.RegistroRequest;
import com.Dev.UvTours.dto.UsuarioRequest;
import com.Dev.UvTours.entity.Rol;
import com.Dev.UvTours.entity.Usuario;
import com.Dev.UvTours.exception.ResourceNotFoundException;
import com.Dev.UvTours.repository.ClienteRepository;
import com.Dev.UvTours.repository.UsuarioRepository;
import com.Dev.UvTours.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        String email = normalizarEmail(request.getEmail());
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setNombre(request.getNombre());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Rol.CLIENTE);

        if (request.getDni() != null && !request.getDni().isBlank()) {
            clienteRepository.findByDni(request.getDni()).ifPresent(usuario::setCliente);
        }

        usuario = usuarioRepository.save(usuario);
        return crearRespuesta(usuario);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return crearRespuesta(usuario);
    }

    @Transactional
    public AuthResponse crearUsuario(UsuarioRequest request) {
        String email = normalizarEmail(request.getEmail());
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setNombre(request.getNombre());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol());
        usuario = usuarioRepository.save(usuario);
        return crearRespuesta(usuario);
    }

    public AuthResponse obtenerActual(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));
        return crearRespuesta(usuario);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private AuthResponse crearRespuesta(Usuario usuario) {
        return new AuthResponse(jwtService.generarToken(usuario), usuario.getEmail(),
                usuario.getRol().name(), usuario.getNombre());
    }
}