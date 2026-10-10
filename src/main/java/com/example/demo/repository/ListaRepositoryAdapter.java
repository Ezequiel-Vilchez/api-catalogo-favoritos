package com.example.demo.repository;

import com.example.demo.model.Lista;
import com.example.demo.model.ListaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository jpaRepository;

    public ListaRepositoryAdapter(ListaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Lista> buscarTodas() {
        return jpaRepository.findAll().stream()
                .map(this::convertirADominio)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Lista> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::convertirADominio);
    }

    @Override
    public Lista guardar(Lista lista) {
        ListaEntity entity = new ListaEntity(lista.id(), lista.nombre());
        ListaEntity guardado = jpaRepository.save(entity);
        return convertirADominio(guardado);
    }

    @Override
    public void eliminar(Long id) {
        jpaRepository.deleteById(id);
    }

    private Lista convertirADominio(ListaEntity entity) {
        return new Lista(entity.getId(), entity.getNombre());
    }
}