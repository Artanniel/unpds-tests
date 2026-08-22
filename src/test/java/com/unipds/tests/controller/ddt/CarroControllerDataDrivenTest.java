package com.unipds.tests.controller.ddt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipds.tests.controller.CarroController;
import com.unipds.tests.model.Carro;
import com.unipds.tests.service.CarroService;

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

@WebMvcTest(CarroController.class)
class CarroControllerDataDrivenTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarroService carroService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- 1. Exemplo com @ValueSource ---
    @ParameterizedTest
    @ValueSource(ints = { 2010, 2018, 2022 })
    @DisplayName("DDT @ValueSource: POST /carros com anos variados deve retornar HTTP 200 OK")
    void shouldCreateCarWithDifferentYears(int ano) throws Exception {
        Carro input = new Carro(null, "Carro Teste", ano);
        Carro output = new Carro(10L, "Carro Teste", ano);

        when(carroService.salvarCarro(any(Carro.class))).thenReturn(output);

        mockMvc.perform(post("/carros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ano", is(ano)));
    }

    // --- 2. Exemplo com @CsvSource ---
    @ParameterizedTest
    @CsvSource({
        "Fiat Cronos, 2020, 1",
        "Chevrolet Tracker, 2023, 2",
        "Renault Kwid, 2019, 3"
    })
    @DisplayName("DDT @CsvSource: POST /carros deve criar carro e retornar JSON com dados do CSV")
    void shouldCreateCarFromCsvData(String modelo, Integer ano, Long returnedId) throws Exception {
        Carro input = new Carro(null, modelo, ano);
        Carro output = new Carro(returnedId, modelo, ano);

        when(carroService.salvarCarro(any(Carro.class))).thenReturn(output);

        mockMvc.perform(post("/carros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(returnedId.intValue())))
                .andExpect(jsonPath("$.modelo", is(modelo)))
                .andExpect(jsonPath("$.ano", is(ano)));
    }

    // --- 3. Exemplo com @MethodSource ---
    @ParameterizedTest
    @MethodSource("provideCarListsForController")
    @DisplayName("DDT @MethodSource: GET /carros deve retornar listas com diferentes tamanhos")
    void shouldGetCarListWithDynamicSize(List<Carro> mockCarList, int expectedSize) throws Exception {
        when(carroService.listarCarros()).thenReturn(mockCarList);

        mockMvc.perform(get("/carros")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(expectedSize)));
    }

    static Stream<Arguments> provideCarListsForController() {
        return Stream.of(
            arguments(List.of(new Carro(1L, "C1", 2020), new Carro(2L, "C2", 2021)), 2),
            arguments(List.of(new Carro(1L, "C3", 2022)), 1),
            arguments(List.of(), 0)
        );
    }

    // --- 4. Exemplo com @ArgumentsSource ---
    @ParameterizedTest
    @ArgumentsSource(CarroControllerArgumentsProvider.class)
    @DisplayName("DDT @ArgumentsSource: POST /carros com provedor de argumentos customizado")
    void shouldCreateCarFromArgumentsSource(Carro input, Carro output) throws Exception {
        when(carroService.salvarCarro(any(Carro.class))).thenReturn(output);

        mockMvc.perform(post("/carros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(output.getId().intValue())))
                .andExpect(jsonPath("$.modelo", is(output.getModelo())));
    }

    static class CarroControllerArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                arguments(new Carro(null, "Peugeot 208", 2022), new Carro(50L, "Peugeot 208", 2022)),
                arguments(new Carro(null, "Citroen C3", 2023), new Carro(60L, "Citroen C3", 2023))
            );
        }
    }
}
