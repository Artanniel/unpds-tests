package com.unipds.tests.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipds.tests.model.Usuario;
import com.unipds.tests.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /usuarios - Deve retornar lista de usuarios com HTTP 200 OK")
    void testListarUsuarios() throws Exception {
        List<Usuario> usuarios = Arrays.asList(
                new Usuario(1L, "João Silva", "joao@email.com"),
                new Usuario(2L, "Maria Oliveira", "maria@email.com")
        );
        when(usuarioService.listarUsuarios()).thenReturn(usuarios);

        mockMvc.perform(get("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("João Silva"))
                .andExpect(jsonPath("$[0].email").value("joao@email.com"))
                .andExpect(jsonPath("$[1].nome").value("Maria Oliveira"));
    }

    @Test
    @DisplayName("POST /usuarios - Deve criar usuario e retornar HTTP 200 OK")
    void testCriarUsuario() throws Exception {
        Usuario usuarioInput = new Usuario(null, "Carlos Souza", "carlos@email.com");
        Usuario usuarioOutput = new Usuario(3L, "Carlos Souza", "carlos@email.com");

        when(usuarioService.salvarUsuario(any(Usuario.class))).thenReturn(usuarioOutput);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nome").value("Carlos Souza"))
                .andExpect(jsonPath("$.email").value("carlos@email.com"));
    }
}
