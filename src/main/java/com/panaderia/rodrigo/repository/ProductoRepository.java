package com.panaderia.rodrigo.repository;

import com.panaderia.rodrigo.model.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto,Long> {

    List<Producto> findByNombreIsContainingIgnoreCase(String nombre);

    List<Producto> findByCategoria(String categoria);

    List<Producto> findByDisponibleTrue();

    List<Producto> findByStockGreaterThan(Integer stock);

    @Query("SELECT p FROM Producto p WHERE p.stock < 10 AND p.disponible = true")
    List<Producto> findByStockBajo();

    @Query("SELECT DISTINCT p.categoria FROM Producto p ORDER BY p.categoria")
    List<Producto> findAllCategoria();

}