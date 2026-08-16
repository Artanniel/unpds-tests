package com.unipds.tests.repository;

import com.unipds.tests.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve salvar e buscar Usuario por ID")
    void testSalvarEBuscarPorId() {
        Usuario usuario = new Usuario(null, "Carlos Lima", "carlos@email.com");
        Usuario salvo = usuarioRepository.save(usuario);

        assertNotNull(salvo.getId());
        assertEquals("Carlos Lima", salvo.getNome());
        assertEquals("carlos@email.com", salvo.getEmail());

        Optional<Usuario> encontrado = usuarioRepository.findById(salvo.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("Carlos Lima", encontrado.get().getNome());
    }

    @Test
    @DisplayName("Deve listar todos os usuarios salvos")
    void testListarTodos() {
        usuarioRepository.save(new Usuario(null, "User One", "user1@email.com"));
        usuarioRepository.save(new Usuario(null, "User Two", "user2@email.com"));

        List<Usuario> usuarios = usuarioRepository.findAll();
        assertFalse(usuarios.isEmpty());
        assertTrue(usuarios.size() >= 2);
    }

    @Test
    @DisplayName("Deve deletar Usuario por ID")
    void testDeletarPorId() {
        Usuario salvo = usuarioRepository.save(new Usuario(null, "User Temp", "temp@email.com"));
        Long id = salvo.getId();

        usuarioRepository.deleteById(id);

        Optional<Usuario> encontrado = usuarioRepository.findById(id);
        assertFalse(encontrado.isPresent());
    }
}
