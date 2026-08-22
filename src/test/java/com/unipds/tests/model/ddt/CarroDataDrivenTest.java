package com.unipds.tests.model.ddt;

import com.unipds.tests.model.Carro;

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

class CarroDataDrivenTest {

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(ints = { 1998, 2005, 2015, 2024 })
    @DisplayName("DDT @ValueSource: Deve atualizar e retornar o ano do carro corretamente")
    void shouldUpdateCarYear(int ano) {
        Carro carro = new Carro();
        carro.setAno(ano);

        assertThat(carro.getAno()).isEqualTo(ano);
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "1, Fiat Uno, 2010",
        "2, Chevrolet Onix, 2021",
        "3, VW Gol, 2018",
        "4, Toyota Corolla, 2023"
    })
    @DisplayName("DDT @CsvSource: Deve instanciar Carro através do construtor completo com dados CSV")
    void shouldInstantiateCarWithCsvData(Long id, String modelo, Integer ano) {
        Carro carro = new Carro(id, modelo, ano);

        assertThat(carro.getId()).isEqualTo(id);
        assertThat(carro.getModelo()).isEqualTo(modelo);
        assertThat(carro.getAno()).isEqualTo(ano);
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("provideModelNames")
    @DisplayName("DDT @MethodSource: Validação de getters e setters de modelo")
    void shouldSetAndGetCarModel(String modeloStr) {
        Carro carro = new Carro();
        carro.setModelo(modeloStr);

        assertThat(carro.getModelo()).isEqualTo(modeloStr);
    }

    static Stream<Arguments> provideModelNames() {
        return Stream.of(
            arguments("Honda Civic"),
            arguments("Hyundai HB20"),
            arguments("Ford Ka")
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(CarroArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: Validação de objetos Carro com IDs e modelos variados")
    void shouldValidateCarInstances(Carro carro, Long expectedId, String expectedModelo, Integer expectedAno) {
        assertThat(carro.getId()).isEqualTo(expectedId);
        assertThat(carro.getModelo()).isEqualTo(expectedModelo);
        assertThat(carro.getAno()).isEqualTo(expectedAno);
    }

    static class CarroArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                arguments(new Carro(10L, "Jeep Compass", 2022), 10L, "Jeep Compass", 2022),
                arguments(new Carro(20L, "Nissan Kicks", 2020), 20L, "Nissan Kicks", 2020)
            );
        }
    }
}
