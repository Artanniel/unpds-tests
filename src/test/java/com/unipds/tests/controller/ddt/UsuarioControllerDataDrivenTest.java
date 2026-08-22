package com.unipds.tests.controller.ddt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipds.tests.controller.UsuarioController;
import com.unipds.tests.model.Usuario;
import com.unipds.tests.service.UsuarioService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerDataDrivenTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(strings = { "user.a@test.com", "user.b@test.com", "user.c@test.com" })
    @DisplayName("DDT @ValueSource: POST /usuarios com diferentes emails deve retornar HTTP 200 OK")
    void shouldCreateUserWithDifferentEmails(String email) throws Exception {
        Usuario input = new Usuario(null, "Usuario Teste", email);
        Usuario output = new Usuario(10L, "Usuario Teste", email);

        when(usuarioService.salvarUsuario(any(Usuario.class))).thenReturn(output);

        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(email)));
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "Gabriel Lima, gabriel@mail.com, 1",
        "Helena Costa, helena@mail.com, 2",
        "Igor Santos, igor@mail.com, 3"
    })
    @DisplayName("DDT @CsvSource: POST /usuarios deve criar usuario e retornar dados do CSV")
    void shouldCreateUserFromCsvData(String nome, String email, Long returnedId) throws Exception {
        Usuario input = new Usuario(null, nome, email);
        Usuario output = new Usuario(returnedId, nome, email);

        when(usuarioService.salvarUsuario(any(Usuario.class))).thenReturn(output);

        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(returnedId.intValue())))
                .andExpect(jsonPath("$.nome", is(nome)))
                .andExpect(jsonPath("$.email", is(email)));
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("provideUserListsForController")
    @DisplayName("DDT @MethodSource: GET /usuarios deve retornar listas com diferentes quantidades")
    void shouldGetUserListWithDynamicSize(List<Usuario> mockUserList, int expectedSize) throws Exception {
        when(usuarioService.listarUsuarios()).thenReturn(mockUserList);

        mockMvc.perform(get("/usuarios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(expectedSize)));
    }

    static Stream<Arguments> provideUserListsForController() {
        return Stream.of(
            arguments(List.of(new Usuario(1L, "U1", "u1@mail.com"), new Usuario(2L, "U2", "u2@mail.com")), 2),
            arguments(List.of(new Usuario(1L, "U3", "u3@mail.com")), 1),
            arguments(List.of(), 0)
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(UsuarioControllerArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: POST /usuarios com provedor customizado de argumentos")
    void shouldCreateUserFromArgumentsSource(Usuario input, Usuario output) throws Exception {
        when(usuarioService.salvarUsuario(any(Usuario.class))).thenReturn(output);

        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(output.getId().intValue())))
                .andExpect(jsonPath("$.nome", is(output.getNome())));
    }

    static class UsuarioControllerArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                arguments(new Usuario(null, "Juliana Paes", "ju@mail.com"), new Usuario(100L, "Juliana Paes", "ju@mail.com")),
                arguments(new Usuario(null, "Lucas Moura", "lucas@mail.com"), new Usuario(200L, "Lucas Moura", "lucas@mail.com"))
            );
        }
    }
}
