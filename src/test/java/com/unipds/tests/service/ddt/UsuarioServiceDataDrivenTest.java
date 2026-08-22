package com.unipds.tests.service.ddt;

import com.unipds.tests.model.Usuario;
import com.unipds.tests.repository.UsuarioRepository;
import com.unipds.tests.service.UsuarioService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceDataDrivenTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(strings = { "Alice", "Bob", "Charlie", "Diana" })
    @DisplayName("DDT @ValueSource: Deve salvar usuários com diferentes nomes")
    void shouldSaveUserWithVariousNames(String nome) {
        Usuario usuarioEntrada = new Usuario(null, nome, nome.toLowerCase() + "@email.com");
        Usuario usuarioSalvo = new Usuario(1L, nome, nome.toLowerCase() + "@email.com");

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        Usuario resultado = usuarioService.salvarUsuario(usuarioEntrada);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo(nome);
        verify(usuarioRepository).save(usuarioEntrada);
    }

    @ParameterizedTest
    @ValueSource(ints = {18, 20, 30, 40, 50, 60, 70})
    @DisplayName("DDT @ValueSource: Deve retornar true para idades habilitadas a votar")
    void valueSourceTest(int idade) {
        assertThat(usuarioService.podeVotar(idade)).isTrue();
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "User 1, user1@domain.com, 10",
        "User 2, user2@domain.com, 20",
        "User 3, user3@domain.com, 30"
    })
    @DisplayName("DDT @CsvSource: Deve processar e salvar usuários definidos em CSV")
    void shouldSaveUsersFromCsv(String nome, String email, Long generatedId) {
        Usuario entrada = new Usuario(null, nome, email);
        Usuario esperado = new Usuario(generatedId, nome, email);

        when(usuarioRepository.save(entrada)).thenReturn(esperado);

        Usuario retorno = usuarioService.salvarUsuario(entrada);

        assertThat(retorno.getId()).isEqualTo(generatedId);
        assertThat(retorno.getNome()).isEqualTo(nome);
        assertThat(retorno.getEmail()).isEqualTo(email);
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("provideUserLists")
    @DisplayName("DDT @MethodSource: Deve listar usuários mockados devolvendo a quantidade esperada")
    void shouldListUsersFromMethodSource(List<Usuario> mockList, int expectedCount) {
        when(usuarioRepository.findAll()).thenReturn(mockList);

        List<Usuario> resultado = usuarioService.listarUsuarios();

        assertThat(resultado).hasSize(expectedCount);
    }

    static Stream<Arguments> provideUserLists() {
        return Stream.of(
            arguments(List.of(new Usuario(1L, "U1", "u1@test.com"), new Usuario(2L, "U2", "u2@test.com")), 2),
            arguments(List.of(new Usuario(1L, "U3", "u3@test.com")), 1),
            arguments(List.of(), 0)
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(UsuarioServiceArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: Validação de salvamento e integridade de Usuario no service")
    void shouldValidateUserSavingWithArgumentsSource(Usuario input, Usuario mockedOutput) {
        when(usuarioRepository.save(input)).thenReturn(mockedOutput);

        Usuario result = usuarioService.salvarUsuario(input);

        assertThat(result.getId()).isEqualTo(mockedOutput.getId());
        assertThat(result.getNome()).isEqualTo(mockedOutput.getNome());
        assertThat(result.getEmail()).isEqualTo(mockedOutput.getEmail());
    }

    static class UsuarioServiceArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            Usuario in1 = new Usuario(null, "Evelyn", "evelyn@test.com");
            Usuario out1 = new Usuario(101L, "Evelyn", "evelyn@test.com");

            Usuario in2 = new Usuario(null, "Fernando", "fernando@test.com");
            Usuario out2 = new Usuario(202L, "Fernando", "fernando@test.com");

            return Stream.of(
                arguments(in1, out1),
                arguments(in2, out2)
            );
        }
    }
}
