package com.panaderia.rodrigo.rest;


import com.panaderia.rodrigo.model.Producto;
import com.panaderia.rodrigo.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    @Autowired
    private ProductoService productoService;

    // GET /api/productos → lista todos
    @GetMapping
    public ResponseEntity<List<Producto>> getAll() {
        return ResponseEntity.ok(productoService.findAll());
    }

    // GET /api/productos/1 → busca por id
    @GetMapping("/{id}")
    public ResponseEntity<Producto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.findById(id));
    }

    // POST /api/productos → crea nuevo
    @PostMapping
    public ResponseEntity<Producto> create(@RequestBody Producto producto) {
        productoService.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    // DELETE /api/productos/1 → elimina
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}