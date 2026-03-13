package com.panaderia.rodrigo.repository;

import com.panaderia.rodrigo.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository  extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByEmail(String email);

    List<Cliente> findByNombreContaining(String nombre);

    boolean existsByEmail(String email);
}
