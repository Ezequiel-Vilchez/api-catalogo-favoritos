package com.example.demo.services;

import com.example.demo.model.Favorito;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.services.dto.FavoritoInputDTO;
import com.example.demo.services.dto.FavoritoOutputDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ProductoService productoService;
    private final ListaRepository listaRepository;

    public FavoritoService(FavoritoRepository favoritoRepository, ProductoService productoService, ListaRepository listaRepository) {
        this.favoritoRepository = favoritoRepository;
        this.productoService = productoService;
        this.listaRepository = listaRepository;
    }

    public FavoritoOutputDTO crearFavorito(FavoritoInputDTO input) {
        productoService.obtenerProductoPorId(input.productoId());

        Favorito nuevoFavorito = new Favorito(
                null, 
                input.productoId(),
                input.notaPersonal(),
                LocalDateTime.now(),
                input.listaId() 
        );

        Favorito guardado = favoritoRepository.guardar(nuevoFavorito);

        return convertirAOutputDTO(guardado);
    }

    public List<FavoritoOutputDTO> listarFavoritos() {
        return favoritoRepository.buscarTodos().stream()
                .map(this::convertirAOutputDTO)
                .collect(Collectors.toList());
    }

    public List<FavoritoOutputDTO> listarFavoritosPorLista(Long listaId) {
        listaRepository.buscarPorId(listaId)
                .orElseThrow(() -> new RuntimeException("Lista no encontrada"));

        return favoritoRepository.buscarPorListaId(listaId).stream()
                .map(this::convertirAOutputDTO)
                .collect(Collectors.toList());
    }

    private FavoritoOutputDTO convertirAOutputDTO(Favorito favorito) {
        return new FavoritoOutputDTO(
                favorito.id(),
                favorito.productoId(),
                favorito.notaPersonal(),
                favorito.fechaAgregado(),
                favorito.listaId() 
        );
    }
    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        if (origenId.equals(destinoId)) {
            throw new IllegalArgumentException("La lista de origen y destino no pueden ser iguales");
        }

        listaRepository.buscarPorId(origenId)
                .orElseThrow(() -> new RuntimeException("Lista origen no encontrada"));
        listaRepository.buscarPorId(destinoId)
                .orElseThrow(() -> new RuntimeException("Lista destino no encontrada"));

        List<Favorito> favoritosOrigen = favoritoRepository.buscarPorListaId(origenId);


        for (Favorito fav : favoritosOrigen) {

            Favorito favoritoActualizado = new Favorito(
                    fav.id(),
                    fav.productoId(),
                    fav.notaPersonal(),
                    fav.fechaAgregado(),
                    destinoId
            );
            
            favoritoRepository.guardar(favoritoActualizado);
        }
    }
}