package com.unipds.tests.service.ddt;

import com.unipds.tests.model.Carro;
import com.unipds.tests.repository.CarroRepository;
import com.unipds.tests.service.CarroService;

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
class CarroServiceDataDrivenTest {

    @Mock
    private CarroRepository carroRepository;

    @InjectMocks
    private CarroService carroService;

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(ints = { 2010, 2015, 2020, 2024 })
    @DisplayName("DDT @ValueSource: Deve salvar carros com diferentes anos no CarroService")
    void shouldSaveCarWithVariousYears(int ano) {
        Carro carroEntrada = new Carro(null, "Modelo Teste", ano);
        Carro carroSalvo = new Carro(1L, "Modelo Teste", ano);

        when(carroRepository.save(any(Carro.class))).thenReturn(carroSalvo);

        Carro resultado = carroService.salvarCarro(carroEntrada);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getAno()).isEqualTo(ano);
        verify(carroRepository).save(carroEntrada);
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "Fiat Palio, 2008, 1",
        "Ford Focus, 2017, 2",
        "Toyota Yaris, 2022, 3"
    })
    @DisplayName("DDT @CsvSource: Deve processar e salvar dados de carros em lote a partir de CSV")
    void shouldSaveCarsFromCsv(String modelo, Integer ano, Long generatedId) {
        Carro entrada = new Carro(null, modelo, ano);
        Carro esperado = new Carro(generatedId, modelo, ano);

        when(carroRepository.save(entrada)).thenReturn(esperado);

        Carro retorno = carroService.salvarCarro(entrada);

        assertThat(retorno.getId()).isEqualTo(generatedId);
        assertThat(retorno.getModelo()).isEqualTo(modelo);
        assertThat(retorno.getAno()).isEqualTo(ano);
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("provideCarLists")
    @DisplayName("DDT @MethodSource: Deve listar carros mockados devolvendo a quantidade correta")
    void shouldListCarsFromMethodSource(List<Carro> mockList, int expectedCount) {
        when(carroRepository.findAll()).thenReturn(mockList);

        List<Carro> resultado = carroService.listarCarros();

        assertThat(resultado).hasSize(expectedCount);
    }

    static Stream<Arguments> provideCarLists() {
        return Stream.of(
            arguments(List.of(new Carro(1L, "Carro A", 2020), new Carro(2L, "Carro B", 2021)), 2),
            arguments(List.of(new Carro(1L, "Carro C", 2022)), 1),
            arguments(List.of(), 0)
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(CarroServiceArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: Validação de salvamento e consistência do objeto Carro retornado")
    void shouldValidateCarSavingWithArgumentsSource(Carro input, Carro mockedOutput) {
        when(carroRepository.save(input)).thenReturn(mockedOutput);

        Carro result = carroService.salvarCarro(input);

        assertThat(result.getId()).isEqualTo(mockedOutput.getId());
        assertThat(result.getModelo()).isEqualTo(mockedOutput.getModelo());
        assertThat(result.getAno()).isEqualTo(mockedOutput.getAno());
    }

    static class CarroServiceArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            Carro in1 = new Carro(null, "Audi A3", 2023);
            Carro out1 = new Carro(100L, "Audi A3", 2023);

            Carro in2 = new Carro(null, "BMW X1", 2024);
            Carro out2 = new Carro(200L, "BMW X1", 2024);

            return Stream.of(
                arguments(in1, out1),
                arguments(in2, out2)
            );
        }
    }
}
