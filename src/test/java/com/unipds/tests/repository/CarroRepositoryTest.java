package com.unipds.tests.repository;

import com.unipds.tests.model.Carro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CarroRepositoryTest {

    @Autowired
    private CarroRepository carroRepository;

    @Test
    @DisplayName("Deve salvar e buscar Carro por ID")
    void testSalvarEBuscarPorId() {
        Carro carro = new Carro(null, "Volkswagen Golf", 2021);
        Carro salvo = carroRepository.save(carro);

        assertNotNull(salvo.getId());
        assertEquals("Volkswagen Golf", salvo.getModelo());
        assertEquals(2021, salvo.getAno());

        Optional<Carro> encontrado = carroRepository.findById(salvo.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("Volkswagen Golf", encontrado.get().getModelo());
    }

    @Test
    @DisplayName("Deve listar todos os carros salvos")
    void testListarTodos() {
        carroRepository.save(new Carro(null, "Car One", 2018));
        carroRepository.save(new Carro(null, "Car Two", 2020));

        List<Carro> carros = carroRepository.findAll();
        assertFalse(carros.isEmpty());
        assertTrue(carros.size() >= 2);
    }

    @Test
    @DisplayName("Deve deletar Carro por ID")
    void testDeletarPorId() {
        Carro salvo = carroRepository.save(new Carro(null, "Car Temp", 2015));
        Long id = salvo.getId();

        carroRepository.deleteById(id);

        Optional<Carro> encontrado = carroRepository.findById(id);
        assertFalse(encontrado.isPresent());
    }
}
