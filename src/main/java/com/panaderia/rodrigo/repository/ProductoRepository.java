package com.panaderia.rodrigo.repository;

import com.panaderia.rodrigo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // JPA ya te da findAll, save, findById, deleteById automáticamente
    List<Producto> findByNombre(String nombre);

    List<Producto> findByCategoria(String categoria);
}