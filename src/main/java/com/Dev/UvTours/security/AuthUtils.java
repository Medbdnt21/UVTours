package com.Dev.UvTours.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

public final class AuthUtils {

    private AuthUtils() {
    }

    public static String emailDe(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        throw new IllegalStateException("Principal de autenticación no soportado");
    }
}