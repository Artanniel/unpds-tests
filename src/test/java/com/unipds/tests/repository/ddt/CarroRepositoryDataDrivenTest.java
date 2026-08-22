package com.unipds.tests.repository.ddt;

import com.unipds.tests.model.Carro;
import com.unipds.tests.repository.CarroRepository;

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
class CarroRepositoryDataDrivenTest {

    @Autowired
    private CarroRepository carroRepository;

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(ints = { 1999, 2012, 2021 })
    @DisplayName("DDT @ValueSource: Deve persistir e recuperar Carro com diferentes anos no banco de dados")
    void shouldSaveAndFindCarByYear(int ano) {
        Carro salvo = carroRepository.save(new Carro(null, "Modelo-" + ano, ano));

        Optional<Carro> encontrado = carroRepository.findById(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getAno()).isEqualTo(ano);
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "Renault Duster, 2019",
        "Chevrolet Spin, 2018",
        "Ford Ranger, 2023"
    })
    @DisplayName("DDT @CsvSource: Deve persistir carros a partir de entradas CSV e verificar campos")
    void shouldSaveCarsFromCsv(String modelo, Integer ano) {
        Carro salvo = carroRepository.save(new Carro(null, modelo, ano));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getModelo()).isEqualTo(modelo);
        assertThat(salvo.getAno()).isEqualTo(ano);
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("provideCarModelsToSave")
    @DisplayName("DDT @MethodSource: Deve salvar e deletar carros mantendo o repositório consistente")
    void shouldSaveAndDeleteCarsFromMethodSource(String modelo, Integer ano) {
        Carro salvo = carroRepository.save(new Carro(null, modelo, ano));
        Long id = salvo.getId();

        carroRepository.deleteById(id);

        assertThat(carroRepository.findById(id)).isEmpty();
    }

    static Stream<Arguments> provideCarModelsToSave() {
        return Stream.of(
            arguments("Toyota Hilux", 2020),
            arguments("Mitsubishi L200", 2021)
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(CarroRepositoryArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: Validação de persistência JPA de entidades Carro")
    void shouldValidateCarJpaPersistence(Carro inputCarro) {
        Carro salvo = carroRepository.save(inputCarro);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getModelo()).isEqualTo(inputCarro.getModelo());
        assertThat(salvo.getAno()).isEqualTo(inputCarro.getAno());
    }

    static class CarroRepositoryArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                arguments(new Carro(null, "Subaru WRX", 2022)),
                arguments(new Carro(null, "Volvo XC60", 2023))
            );
        }
    }
}
