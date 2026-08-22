package com.unipds.tests.service.ddt;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.logging.Logger;

public class DataFakerExampleTest {

    @Test
    void exemplosDataFaker() {
        Faker faker = new Faker(Locale.forLanguageTag("pt-BR"));

        var fullName = faker.name().fullName();
        var address = faker.address().fullAddress();
        var cpf = faker.cpf().valid(true);
        var username = faker.credentials().username();
        var password = faker.credentials().password(12, 24);

        Logger.getAnonymousLogger().info(fullName);
        Logger.getAnonymousLogger().info(address);
        Logger.getAnonymousLogger().info(cpf);
        Logger.getAnonymousLogger().info(username);
        Logger.getAnonymousLogger().info(password);
        Logger.getAnonymousLogger().info(
                "================================================================================================");
    }

    @Test
    void exemplosDataFakerJapanese() {
        Faker faker = new Faker(Locale.JAPAN);

        var fullName = faker.name().fullName();
        var address = faker.address().fullAddress();
        var cpf = faker.cpf().valid(true);
        var username = faker.credentials().username();
        var password = faker.credentials().password(12, 24);

        Logger.getAnonymousLogger().info(fullName);
        Logger.getAnonymousLogger().info(address);
        Logger.getAnonymousLogger().info(cpf);
        Logger.getAnonymousLogger().info(username);
        Logger.getAnonymousLogger().info(password);
        Logger.getAnonymousLogger().info(
                "================================================================================================");
    }

    @Test
    void exemplosDataFakerUSA() {
        Faker faker = new Faker(Locale.US);

        var fullName = faker.name().fullName();
        var address = faker.address().fullAddress();
        var cpf = faker.cpf().valid(true);
        var username = faker.credentials().username();
        var password = faker.credentials().password(12, 24);

        Logger.getAnonymousLogger().info(fullName);
        Logger.getAnonymousLogger().info(address);
        Logger.getAnonymousLogger().info(cpf);
        Logger.getAnonymousLogger().info(username);
        Logger.getAnonymousLogger().info(password);
        Logger.getAnonymousLogger().info(
                "================================================================================================");
    }
}
