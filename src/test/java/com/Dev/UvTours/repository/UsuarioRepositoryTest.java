package com.Dev.UvTours.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.Dev.UvTours.entity.Rol;
import com.Dev.UvTours.entity.Usuario;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindByEmail() {
        // Given
        Usuario usuario = persistirUsuario("juan@email.com");

        // When
        var encontrado = usuarioRepository.findByEmail("juan@email.com");

        // Then
        assertTrue(encontrado.isPresent());
        assertEquals(usuario.getId(), encontrado.get().getId());
        assertEquals(Rol.CLIENTE, encontrado.get().getRol());
    }

    @Test
    void testFindByEmail_NoExiste() {
        // When & Then
        assertTrue(usuarioRepository.findByEmail("noexiste@email.com").isEmpty());
    }

    @Test
    void testExistsByEmail() {
        // Given
        persistirUsuario("maria@email.com");

        // When & Then
        assertTrue(usuarioRepository.existsByEmail("maria@email.com"));
        assertFalse(usuarioRepository.existsByEmail("otro@email.com"));
    }

    @Test
    void testNoPermiteEmailDuplicado() {
        // Given
        persistirUsuario("repetido@email.com");

        Usuario duplicado = new Usuario();
        duplicado.setEmail("repetido@email.com");
        duplicado.setNombre("Otro");
        duplicado.setPasswordHash("hash");
        duplicado.setRol(Rol.ADMIN);

        // When & Then
        assertThrows(Exception.class, () -> {
            entityManager.persist(duplicado);
            entityManager.flush();
        });
    }

    private Usuario persistirUsuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setNombre("Juan Pérez");
        usuario.setPasswordHash("$2a$10$hash");
        usuario.setRol(Rol.CLIENTE);
        entityManager.persist(usuario);
        entityManager.flush();
        return usuario;
    }
}