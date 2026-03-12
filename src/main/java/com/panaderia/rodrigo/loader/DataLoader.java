package com.panaderia.rodrigo.loader;

import com.panaderia.rodrigo.model.Producto;
import com.panaderia.rodrigo.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public void run(String... args) {
        productoRepository.save(new Producto(null, "MediasLunas", "Facturas", 35.0, 5));
        productoRepository.save(new Producto(null, "Marraqueta", "Pan", 120.0, 150));
        productoRepository.save(new Producto(null, "Torta de Chocolate", "Tortas", 2800.0, 50));
    }
}
