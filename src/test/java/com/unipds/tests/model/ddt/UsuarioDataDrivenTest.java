package com.unipds.tests.model.ddt;

import com.unipds.tests.model.Usuario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class UsuarioDataDrivenTest {

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(strings = { "joao@email.com", "maria@empresa.org", "dev@unipds.edu.br" })
    @DisplayName("DDT @ValueSource: Deve atribuir emails válidos para Usuario")
    void shouldSetAndGetEmail(String email) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        assertThat(usuario.getEmail()).isEqualTo(email);
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "1, João Silva, joao.silva@gmail.com",
        "2, Maria Oliveira, maria.o@outlook.com",
        "3, Carlos Eduardo, carlos.edu@tech.io"
    })
    @DisplayName("DDT @CsvSource: Deve instanciar Usuario com dados em formato CSV")
    void shouldInstantiateUserWithCsv(Long id, String nome, String email) {
        Usuario usuario = new Usuario(id, nome, email);

        assertThat(usuario.getId()).isEqualTo(id);
        assertThat(usuario.getNome()).isEqualTo(nome);
        assertThat(usuario.getEmail()).isEqualTo(email);
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("provideUserNames")
    @DisplayName("DDT @MethodSource: Validação de alteração de nome do usuário")
    void shouldSetAndGetNome(String nome) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);

        assertThat(usuario.getNome()).isEqualTo(nome);
    }

    static Stream<Arguments> provideUserNames() {
        return Stream.of(
            arguments("Ana Paula"),
            arguments("Bruno Santos"),
            arguments("Carla Souza")
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(UsuarioArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: Validação de objetos Usuario preenchidos")
    void shouldValidateUserInstances(Usuario usuario, Long expectedId, String expectedNome, String expectedEmail) {
        assertThat(usuario.getId()).isEqualTo(expectedId);
        assertThat(usuario.getNome()).isEqualTo(expectedNome);
        assertThat(usuario.getEmail()).isEqualTo(expectedEmail);
    }

    static class UsuarioArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                arguments(new Usuario(100L, "Daniel Lima", "daniel@test.com"), 100L, "Daniel Lima", "daniel@test.com"),
                arguments(new Usuario(200L, "Elena Rostova", "elena@test.com"), 200L, "Elena Rostova", "elena@test.com")
            );
        }
    }
}
