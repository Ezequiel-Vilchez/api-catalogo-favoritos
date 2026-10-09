package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favoritos") 
public class FavoritoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private String nota;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;


    public FavoritoEntity() {}

    public FavoritoEntity(Long id, Long productoId, String nota, LocalDateTime fechaAlta) {
        this.id = id;
        this.productoId = productoId;
        this.nota = nota;
        this.fechaAlta = fechaAlta;
    }


    public Long getId() { return id; }
    public Long getProductoId() { return productoId; }
    public String getNota() { return nota; }
    public LocalDateTime getFechaAlta() { return fechaAlta; }
}