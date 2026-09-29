package com.Dev.UvTours.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtService jwtService;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final ObjectMapper objectMapper;

    @Value("${app.oauth2.enabled:false}")
    private boolean oauth2Enabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/viajes", "/api/actividades", "/api/actividades/hoy")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/compras/mias").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.POST, "/api/compras/mias").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/api/inscripciones/mias").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.POST, "/api/inscripciones/mias").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.DELETE, "/api/inscripciones/mias/**").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.POST, "/api/auth/usuarios").hasRole("ADMIN")
                .requestMatchers("/api/viajes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/tiendas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tiendas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/tiendas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/empleados/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/empleados/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/empleados/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/tiendas/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers(HttpMethod.GET, "/api/empleados/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/clientes/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/compras/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/inscripciones/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers(HttpMethod.PATCH, "/api/actividades/*/estado").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers(HttpMethod.POST, "/api/actividades/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/actividades/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/actividades/**").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated())
                .oauth2ResourceServer(oAuth2 -> oAuth2.jwt(jwt -> jwt.decoder(jwtService.getDecoder())))
                .exceptionHandling(ex -> ex
                .authenticationEntryPoint(restAuthenticationEntryPoint())
                .accessDeniedHandler(restAccessDeniedHandler()));

        if (oauth2Enabled) {
            http.oauth2Login(oauth2 -> oauth2.successHandler(oauth2LoginSuccessHandler));
        }

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    private AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) -> escribirError(response, HttpServletResponse.SC_UNAUTHORIZED,
                "No autenticado: se requiere un token JWT válido");
    }

    private AccessDeniedHandler restAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> escribirError(response, HttpServletResponse.SC_FORBIDDEN,
                "Acceso denegado: el rol no tiene permisos sobre este recurso");
    }

    private void escribirError(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                Map.of("timestamp", LocalDateTime.now(), "message", mensaje, "status", status));
    }
}
