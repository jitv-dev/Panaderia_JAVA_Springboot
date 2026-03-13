package com.panaderia.rodrigo.service;

import com.panaderia.rodrigo.model.Producto;
import com.panaderia.rodrigo.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public void save(Producto producto) {
        productoRepository.save(producto);
    }

    public Producto findById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public void delete(Long id) {
        productoRepository.deleteById(id);
    }
}
