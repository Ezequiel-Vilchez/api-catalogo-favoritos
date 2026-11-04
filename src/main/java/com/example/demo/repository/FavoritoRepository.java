package com.example.demo.repository;

import com.example.demo.model.Favorito;
import java.util.List;
import java.util.Optional;

public interface FavoritoRepository {
    List<Favorito> buscarTodos();
    Optional<Favorito> buscarPorId(Long id);
    List<Favorito> buscarPorListaId(Long listaId);
    Favorito guardar(Favorito favorito);
    void eliminarPorId(Long id);
    void moverFavoritos(Long origenId, Long destinoId);
}