package com.panaderia.rodrigo.repository;

import com.panaderia.rodrigo.model.Pedido;
import com.panaderia.rodrigo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PedidoRepository  extends JpaRepository<Pedido,Long> {

    List<Pedido> findByClienteID(Long ClienteID);

    List<Pedido> findEstado(Pedido.EstadoPedido estado);

    @Query("SELECT p FROM Pedido p ORDER BYE p.fechaPedido DESC")
    List<Pedido> findAllOrderByFechaDesc();

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.estado = :estado")
    Long countByEstado(Pedido.EstadoPedido estado);
}
