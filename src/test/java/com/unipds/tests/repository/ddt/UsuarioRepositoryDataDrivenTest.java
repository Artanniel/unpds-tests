package com.unipds.tests.repository.ddt;

import com.unipds.tests.model.Usuario;
import com.unipds.tests.repository.UsuarioRepository;

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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryDataDrivenTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(strings = { "alpha@test.com", "beta@test.com", "gamma@test.com" })
    @DisplayName("DDT @ValueSource: Deve persistir Usuario com diferentes emails")
    void shouldSaveAndFindUserByEmail(String email) {
        Usuario salvo = usuarioRepository.save(new Usuario(null, "User " + email, email));

        Optional<Usuario> encontrado = usuarioRepository.findById(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEmail()).isEqualTo(email);
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "Marcos Rocha, marcos@domain.com",
        "Patricia Ramos, patricia@domain.com",
        "Renato Augusto, renato@domain.com"
    })
    @DisplayName("DDT @CsvSource: Deve salvar usuários com dados fornecidos via CSV")
    void shouldSaveUsersFromCsv(String nome, String email) {
        Usuario salvo = usuarioRepository.save(new Usuario(null, nome, email));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo(nome);
        assertThat(salvo.getEmail()).isEqualTo(email);
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("provideUserDataToSave")
    @DisplayName("DDT @MethodSource: Deve salvar e depois excluir usuários do banco de dados")
    void shouldSaveAndDeleteUsersFromMethodSource(String nome, String email) {
        Usuario salvo = usuarioRepository.save(new Usuario(null, nome, email));
        Long id = salvo.getId();

        usuarioRepository.deleteById(id);

        assertThat(usuarioRepository.findById(id)).isEmpty();
    }

    static Stream<Arguments> provideUserDataToSave() {
        return Stream.of(
            arguments("Samuel Rosa", "samuel@rock.com"),
            arguments("Thiago Silva", "thiago@football.com")
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(UsuarioRepositoryArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: Validação de persistência JPA para entidades Usuario")
    void shouldValidateUserJpaPersistence(Usuario inputUsuario) {
        Usuario salvo = usuarioRepository.save(inputUsuario);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo(inputUsuario.getNome());
        assertThat(salvo.getEmail()).isEqualTo(inputUsuario.getEmail());
    }

    static class UsuarioRepositoryArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                arguments(new Usuario(null, "Vanessa da Mata", "vanessa@music.com")),
                arguments(new Usuario(null, "Wagner Moura", "wagner@cinema.com"))
            );
        }
    }
}
