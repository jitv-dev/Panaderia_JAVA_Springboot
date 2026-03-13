package com.panaderia.rodrigo.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pedido_productos",
            joinColumns = @JoinColumn(name = "pedido_id"),
            inverseJoinColumns = @JoinColumn(name = "producto_id")
    )
    private List<Producto> productos;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDateTime fechaPedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Column(name = "total")
    private Double total;

    @Size(max = 300)
    private String observaciones;

    public enum EstadoPedido {
        PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaPedido == null) {
            this.fechaPedido = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoPedido.PENDIENTE;
        }
    }
}
