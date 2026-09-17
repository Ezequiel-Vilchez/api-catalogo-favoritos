package com.example.demo.repository;

import com.example.demo.model.Favorito;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class FavoritoRepositoryMemoria implements FavoritoRepository {
    
    private final Map<Long, Favorito> datos = new ConcurrentHashMap<>();
    
    private final AtomicLong secuencia = new AtomicLong();

    @Override
    public List<Favorito> buscarTodos() {
        return new ArrayList<>(datos.values());
    }

    @Override
    public Optional<Favorito> buscarPorId(Long id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public Favorito guardar(Favorito favorito) {
        
        Long id = favorito.id() == null ? secuencia.incrementAndGet() : favorito.id();
        
        Favorito guardado = new Favorito(
            id, 
            favorito.productoId(), 
            favorito.notaPersonal(), 
            favorito.fechaAgregado()
        );
        
        datos.put(id, guardado);
        return guardado;
    }

    @Override
    public void eliminar(Long id) {
        datos.remove(id);
    }
}
