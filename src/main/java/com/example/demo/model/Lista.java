package com.example.demo.model;

import java.util.List;

public record Lista(
    Long id,
    String nombre,
    List<Favorito> favoritos,
    Usuario usuario
) {
    public Lista(Long id, String nombre, List<Favorito> favoritos) {
        this(id, nombre, favoritos, null);
    }
}