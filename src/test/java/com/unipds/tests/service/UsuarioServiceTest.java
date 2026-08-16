package com.unipds.tests.service;

import com.unipds.tests.model.Usuario;
import com.unipds.tests.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve listar todos os usuarios via Service")
    void testListarUsuarios() {
        List<Usuario> listaMock = Arrays.asList(
                new Usuario(1L, "João", "joao@test.com"),
                new Usuario(2L, "Maria", "maria@test.com")
        );
        when(usuarioRepository.findAll()).thenReturn(listaMock);

        List<Usuario> resultado = usuarioService.listarUsuarios();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("João", resultado.get(0).getNome());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve salvar usuario via Service")
    void testSalvarUsuario() {
        Usuario usuarioParaSalvar = new Usuario(null, "Ana", "ana@test.com");
        Usuario usuarioSalvo = new Usuario(10L, "Ana", "ana@test.com");

        when(usuarioRepository.save(usuarioParaSalvar)).thenReturn(usuarioSalvo);

        Usuario resultado = usuarioService.salvarUsuario(usuarioParaSalvar);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals("Ana", resultado.getNome());
        verify(usuarioRepository, times(1)).save(usuarioParaSalvar);
    }
}
