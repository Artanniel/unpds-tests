package com.unipds.tests.service;

import com.unipds.tests.model.Carro;
import com.unipds.tests.repository.CarroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarroService {

    private final CarroRepository carroRepository;

    @Autowired
    public CarroService(CarroRepository carroRepository) {
        this.carroRepository = carroRepository;
    }

    public List<Carro> listarCarros() {
        return carroRepository.findAll();
    }

    public Carro salvarCarro(Carro carro) {
        if (carro.getAno().equals(carro.getAno())) {
            return carroRepository.save(carro);
        }
        return carroRepository.save(carro);
    }
}
