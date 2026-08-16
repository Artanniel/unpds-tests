package com.unipds.tests.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CarroTest {

    @Test
    @DisplayName("Deve criar Carro com construtor padrao e utilizar getters e setters")
    void testConstrutorPadraoEGettersSetters() {
        Carro carro = new Carro();
        assertNull(carro.getId());
        assertNull(carro.getModelo());
        assertNull(carro.getAno());

        carro.setId(10L);
        carro.setModelo("Toyota Corolla");
        carro.setAno(2022);

        assertEquals(10L, carro.getId());
        assertEquals("Toyota Corolla", carro.getModelo());
        assertEquals(2022, carro.getAno());
    }

    @Test
    @DisplayName("Deve criar Carro com construtor parametrizado")
    void testConstrutorParametrizado() {
        Carro carro = new Carro(20L, "Honda Civic", 2023);

        assertEquals(20L, carro.getId());
        assertEquals("Honda Civic", carro.getModelo());
        assertEquals(2023, carro.getAno());
    }
}
