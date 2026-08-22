package com.unipds.tests.factory;

import com.unipds.tests.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class UserDataFactoryTest {

    private static final Logger LOGGER = Logger.getLogger(UserDataFactoryTest.class.getName());

    @Test
    @DisplayName("Deve gerar um usuário válido utilizando o Test Data Factory e Datafaker")
    void deveGerarUsuarioValidoComDataFactory() {
        User usuario = UserDataFactory.usuarioValido();

        assertThat(usuario).isNotNull();
        assertThat(usuario.getNome()).isNotBlank();
        assertThat(usuario.getEmail()).contains("@");
        assertThat(usuario.getSenha()).hasSizeBetween(8, 16);
        assertThat(usuario.getEndereco()).isNotBlank();

        LOGGER.info("=== Test Data Factory: Usuário Gerado ===");
        LOGGER.info("Nome: " + usuario.getNome());
        LOGGER.info("Email: " + usuario.getEmail());
        LOGGER.info("Senha: " + usuario.getSenha());
        LOGGER.info("Endereço: " + usuario.getEndereco());
    }

    @Test
    @DisplayName("Deve gerar um usuário estático pré-definido")
    void deveGerarUsuarioEstaticoComDataFactory() {
        User usuario = UserDataFactory.usuarioEstatico();

        assertThat(usuario.getNome()).isEqualTo("Joao da Silva");
        assertThat(usuario.getEmail()).isEqualTo("joao.dasilva@gmail.com");
        assertThat(usuario.getSenha()).isEqualTo("1q2w3e4r5t6y");
        assertThat(usuario.getEndereco()).isEqualTo("Avenida Brasil, 5000, Rio de Janeiro");
    }
}
