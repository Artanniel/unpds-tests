package com.unipds.tests.controller;

import com.unipds.tests.model.Carro;
import com.unipds.tests.service.CarroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carros")
public class CarroController {

    private final CarroService carroService;

    @Autowired
    public CarroController(CarroService carroService) {
        this.carroService = carroService;
    }

    @GetMapping
    public List<Carro> listarCarros() {
        return carroService.listarCarros();
    }

    @PostMapping
    public Carro criarCarro(@RequestBody Carro carro) {
        return carroService.salvarCarro(carro);
    }
}
