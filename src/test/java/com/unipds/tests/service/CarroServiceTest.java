package com.unipds.tests.service;

import com.unipds.tests.model.Carro;
import com.unipds.tests.repository.CarroRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarroServiceTest {

    @Mock
    private CarroRepository carroRepository;

    @InjectMocks
    private CarroService carroService;

    @Test
    @DisplayName("Deve listar todos os carros via Service")
    void testListarCarros() {
        List<Carro> listaMock = Arrays.asList(
                new Carro(1L, "Fiat Uno", 2010),
                new Carro(2L, "Chevrolet Onix", 2020)
        );
        when(carroRepository.findAll()).thenReturn(listaMock);

        List<Carro> resultado = carroService.listarCarros();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Fiat Uno", resultado.get(0).getModelo());
        verify(carroRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve salvar carro via Service")
    void testSalvarCarro() {
        Carro carroParaSalvar = new Carro(null, "Hyundai HB20", 2022);
        Carro carroSalvo = new Carro(5L, "Hyundai HB20", 2022);

        when(carroRepository.save(carroParaSalvar)).thenReturn(carroSalvo);

        Carro resultado = carroService.salvarCarro(carroParaSalvar);

        assertNotNull(resultado);
        assertEquals(5L, resultado.getId());
        assertEquals("Hyundai HB20", resultado.getModelo());
        verify(carroRepository, times(1)).save(carroParaSalvar);
    }
}
