package com.example.demo.repository;

import com.example.demo.model.Favorito;
import com.example.demo.model.FavoritoEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository 
public class FavoritoRepositoryAdapter implements FavoritoRepository {

    private final FavoritoJpaRepository jpaRepository;

    public FavoritoRepositoryAdapter(FavoritoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Favorito> buscarTodos() {
        return jpaRepository.findAll().stream()
                .map(this::convertirADominio)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Favorito> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::convertirADominio);
    }

    @Override
    public Favorito guardar(Favorito favorito) {

        FavoritoEntity entity = new FavoritoEntity(
                favorito.id(),
                favorito.productoId(),
                favorito.notaPersonal(),
                favorito.fechaAgregado()
        );
        

        FavoritoEntity guardado = jpaRepository.save(entity);
        

        return convertirADominio(guardado);
    }

    @Override
    public void eliminar(Long id) {
        jpaRepository.deleteById(id);
    }

    private Favorito convertirADominio(FavoritoEntity entity) {
        return new Favorito(
                entity.getId(),
                entity.getProductoId(),
                entity.getNota(),
                entity.getFechaAlta()
        );
    }
}