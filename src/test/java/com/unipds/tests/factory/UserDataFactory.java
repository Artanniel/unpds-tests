package com.unipds.tests.factory;

import com.unipds.tests.model.User;
import net.datafaker.Faker;

import java.util.Locale;

/**
 * Test Data Factory para geração de instâncias da entidade/modelo User.
 * Combina o modelo (User), o padrão Factory e a ferramenta Datafaker.
 */
public final class UserDataFactory {

    private static final Faker faker = new Faker(Locale.forLanguageTag("pt-BR"));

    private UserDataFactory() {
    }

    /**
     * Gera um usuário válido com dados aleatórios em português do Brasil.
     */
    public static User usuarioValido() {
        return User.builder()
                .nome(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .senha(faker.credentials().password(8, 16))
                .endereco(faker.address().fullAddress())
                .build();
    }

    /**
     * Gera um usuário estático para cenários que exigem previsibilidade.
     */
    public static User usuarioEstatico() {
        return User.builder()
                .nome("Joao da Silva")
                .email("joao.dasilva@gmail.com")
                .senha("1q2w3e4r5t6y")
                .endereco("Avenida Brasil, 5000, Rio de Janeiro")
                .build();
    }
}
