package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "listas")
public class ListaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    // Constructor vacío exigido por JPA
    public ListaEntity() {}

    // Constructor con parámetros (el que necesita el Adapter)
    public ListaEntity(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getters (el getId() que te marcaba en rojo)
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}