package com.Dev.UvTours.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Dev.UvTours.dto.AuthResponse;
import com.Dev.UvTours.dto.LoginRequest;
import com.Dev.UvTours.dto.RegistroRequest;
import com.Dev.UvTours.dto.UsuarioRequest;
import com.Dev.UvTours.security.AuthUtils;
import com.Dev.UvTours.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.ok(authService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<AuthResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(authService.crearUsuario(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> obtenerActual(Authentication authentication) {
        return ResponseEntity.ok(authService.obtenerActual(AuthUtils.emailDe(authentication)));
    }
}