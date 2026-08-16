package com.unipds.tests.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UsuarioTest {

    @Test
    @DisplayName("Deve criar Usuario com construtor padrao e utilizar getters e setters")
    void testConstrutorPadraoEGettersSetters() {
        Usuario usuario = new Usuario();
        assertNull(usuario.getId());
        assertNull(usuario.getNome());
        assertNull(usuario.getEmail());

        usuario.setId(1L);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");

        assertEquals(1L, usuario.getId());
        assertEquals("João Silva", usuario.getNome());
        assertEquals("joao@email.com", usuario.getEmail());
    }

    @Test
    @DisplayName("Deve criar Usuario com construtor parametrizado")
    void testConstrutorParametrizado() {
        Usuario usuario = new Usuario(2L, "Maria Oliveira", "maria@email.com");

        assertEquals(2L, usuario.getId());
        assertEquals("Maria Oliveira", usuario.getNome());
        assertEquals("maria@email.com", usuario.getEmail());
    }
}
