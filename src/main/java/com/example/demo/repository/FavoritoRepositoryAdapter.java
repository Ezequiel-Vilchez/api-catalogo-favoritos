package com.example.demo.repository;

import com.example.demo.model.Favorito;
import com.example.demo.model.FavoritoEntity;
import com.example.demo.model.ListaEntity;
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
        // Armamos la referencia a la lista
        ListaEntity listaRef = null;
        if (favorito.listaId() != null) {
            listaRef = new ListaEntity(favorito.listaId(), null);
        }

        FavoritoEntity entity = new FavoritoEntity(
                favorito.id(),
                favorito.productoId(),
                favorito.notaPersonal(),
                favorito.fechaAgregado(),
                listaRef);

        FavoritoEntity guardado = jpaRepository.save(entity);
        return convertirADominio(guardado);
    }

    @Override
    public void eliminar(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Favorito> buscarPorListaId(Long listaId) {
        return jpaRepository.findByListaId(listaId).stream()
                .map(this::convertirADominio)
                .collect(Collectors.toList());
    }

    private Favorito convertirADominio(FavoritoEntity entity) {
        Long listaId = (entity.getLista() != null) ? entity.getLista().getId() : null;

        return new Favorito(
                entity.getId(),
                entity.getProductoId(),
                entity.getNota(),
                entity.getFechaAlta(),
                listaId);
    }
}