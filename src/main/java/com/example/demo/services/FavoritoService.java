package com.example.demo.services;

import com.example.demo.model.Favorito;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.services.dto.FavoritoInputDTO;
import com.example.demo.services.dto.FavoritoOutputDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Los favoritos cuelgan de una lista, y la lista pertenece a un usuario.
 * Por eso toda operación sobre favoritos primero pasa por ListaService.obtenerListaPorId,
 * que es donde vive la regla de autorización (404 si no existe, 403 si no es tuya ni sos ADMIN).
 * De esta forma la regla está en un solo lugar y no se duplica.
 */
@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ProductoService productoService;
    private final ListaService listaService;

    public FavoritoService(FavoritoRepository favoritoRepository, ProductoService productoService, ListaService listaService) {
        this.favoritoRepository = favoritoRepository;
        this.productoService = productoService;
        this.listaService = listaService;
    }

    public FavoritoOutputDTO crearFavorito(FavoritoInputDTO input) {
        // Valida que la lista exista y sea del usuario autenticado (o que sea ADMIN)
        listaService.obtenerListaPorId(input.listaId());

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

    /**
     * Devuelve los favoritos de las listas visibles para el usuario autenticado:
     * solo las suyas si es USER, todas si es ADMIN.
     */
    public List<FavoritoOutputDTO> listarFavoritos() {
        return listaService.listarListas().stream()
                .flatMap(lista -> favoritoRepository.buscarPorListaId(lista.id()).stream())
                .map(this::convertirAOutputDTO)
                .toList();
    }

    public List<FavoritoOutputDTO> listarFavoritosPorLista(Long listaId) {
        listaService.obtenerListaPorId(listaId);

        return favoritoRepository.buscarPorListaId(listaId).stream()
                .map(this::convertirAOutputDTO)
                .toList();
    }

    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        if (origenId.equals(destinoId)) {
            throw new IllegalArgumentException("La lista de origen y destino no pueden ser iguales");
        }

        // Ambas listas tienen que existir y ser del usuario autenticado (o ser ADMIN)
        listaService.obtenerListaPorId(origenId);
        listaService.obtenerListaPorId(destinoId);

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

    private FavoritoOutputDTO convertirAOutputDTO(Favorito favorito) {
        return new FavoritoOutputDTO(
                favorito.id(),
                favorito.productoId(),
                favorito.notaPersonal(),
                favorito.fechaAgregado(),
                favorito.listaId()
        );
    }
}