package com.panaderia.rodrigo.model;


import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String categoria;
    private Double precio;
    private Integer stock;
}
