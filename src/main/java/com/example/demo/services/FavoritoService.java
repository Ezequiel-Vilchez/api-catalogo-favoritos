package com.example.demo.services;

import com.example.demo.model.Favorito;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository; // <-- 1. IMPORT AGREGADO
import com.example.demo.services.dto.FavoritoInputDTO;
import com.example.demo.services.dto.FavoritoOutputDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ProductoService productoService;
    private final ListaRepository listaRepository;

    // 2. CONSTRUCTOR ACTUALIZADO (ahora pide los 3 parámetros)
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
                null // <-- 3. QUINTO PARÁMETRO AGREGADO (listaId)
        );

        Favorito guardado = favoritoRepository.guardar(nuevoFavorito);

        return convertirAOutputDTO(guardado);
    }

    public List<FavoritoOutputDTO> listarFavoritos() {
        return favoritoRepository.buscarTodos().stream()
                .map(this::convertirAOutputDTO)
                .collect(Collectors.toList());
    }

    private FavoritoOutputDTO convertirAOutputDTO(Favorito favorito) {
        return new FavoritoOutputDTO(
                favorito.id(),
                favorito.productoId(),
                favorito.notaPersonal(),
                favorito.fechaAgregado()
        );
    }
}