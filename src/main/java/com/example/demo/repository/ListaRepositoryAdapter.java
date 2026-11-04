package com.example.demo.repository;

import com.example.demo.model.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository jpaRepository;

    public ListaRepositoryAdapter(ListaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Lista guardar(Lista lista) {
        UsuarioEntity usuarioEntity = null;
        if (lista.usuario() != null) {
            usuarioEntity = new UsuarioEntity(
                lista.usuario().id(),
                lista.usuario().email(),
                lista.usuario().passwordHash(),
                lista.usuario().rol()
            );
        }

        ListaEntity entity = new ListaEntity(lista.id(), lista.nombre(), usuarioEntity);
        ListaEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public List<Lista> obtenerTodas() {
        return jpaRepository.findAll().stream().map(this::mapToDomain).toList();
    }

    @Override
    public List<Lista> obtenerPorUsuarioId(Long usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId).stream().map(this::mapToDomain).toList();
    }

    @Override
    public Optional<Lista> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public void eliminarPorId(Long id) {
        jpaRepository.deleteById(id);
    }

    private Lista mapToDomain(ListaEntity entity) {
        List<Favorito> favs = entity.getFavoritos() == null ? List.of() :
                entity.getFavoritos().stream()
                        .map(f -> new Favorito(f.getId(), f.getProductoId(), f.getNotaPersonal(), f.getFechaAgregado(), entity.getId()))
                        .toList();

        Usuario usuario = null;
        if (entity.getUsuario() != null) {
            usuario = new Usuario(entity.getUsuario().getId(), entity.getUsuario().getEmail(), entity.getUsuario().getPasswordHash(), entity.getUsuario().getRol());
        }

        return new Lista(entity.getId(), entity.getNombre(), favs, usuario);
    }
}