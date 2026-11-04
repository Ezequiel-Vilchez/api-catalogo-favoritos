package com.example.demo.services.dto;

import com.example.demo.model.Lista;
import java.util.List;

public record ListaOutputDTO(
    Long id,
    String nombre,
    List<FavoritoOutputDTO> favoritos
) {
    public static ListaOutputDTO desdeDominio(Lista lista) {
        List<FavoritoOutputDTO> favs = (lista.favoritos() == null) ? List.of() :
                lista.favoritos().stream()
                        .map(f -> new FavoritoOutputDTO(f.id(), f.productoId(), f.notaPersonal(), f.fechaAgregado(), f.listaId()))
                        .toList();
        return new ListaOutputDTO(lista.id(), lista.nombre(), favs);
    }
}