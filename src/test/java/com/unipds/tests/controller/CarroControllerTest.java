package com.unipds.tests.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipds.tests.model.Carro;
import com.unipds.tests.service.CarroService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarroController.class)
class CarroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarroService carroService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /carros - Deve retornar lista de carros com HTTP 200 OK")
    void testListarCarros() throws Exception {
        List<Carro> carros = Arrays.asList(
                new Carro(1L, "Toyota Corolla", 2020),
                new Carro(2L, "Honda Civic", 2019)
        );
        when(carroService.listarCarros()).thenReturn(carros);

        mockMvc.perform(get("/carros")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].modelo").value("Toyota Corolla"))
                .andExpect(jsonPath("$[0].ano").value(2020))
                .andExpect(jsonPath("$[1].modelo").value("Honda Civic"));
    }

    @Test
    @DisplayName("POST /carros - Deve criar carro e retornar HTTP 200 OK")
    void testCriarCarro() throws Exception {
        Carro carroInput = new Carro(null, "Ford Mustang", 2021);
        Carro carroOutput = new Carro(3L, "Ford Mustang", 2021);

        when(carroService.salvarCarro(any(Carro.class))).thenReturn(carroOutput);

        mockMvc.perform(post("/carros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carroInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.modelo").value("Ford Mustang"))
                .andExpect(jsonPath("$.ano").value(2021));
    }
}
