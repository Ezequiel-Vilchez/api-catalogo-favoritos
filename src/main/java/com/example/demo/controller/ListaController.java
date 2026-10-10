package com.example.demo.controller;

import com.example.demo.services.FavoritoService;
import com.example.demo.services.ListaService;
import com.example.demo.services.dto.ListaInputDTO;
import com.example.demo.services.dto.ListaOutputDTO;
import com.example.demo.services.dto.MoverFavoritosInputDTO;
import com.example.demo.services.dto.FavoritoOutputDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listas")
public class ListaController {

    private final ListaService listaService;
    private final FavoritoService favoritoService; // <-- Se agregó esta dependencia

    // Se actualizó el constructor para recibir ambos servicios
    public ListaController(ListaService listaService, FavoritoService favoritoService) {
        this.listaService = listaService;
        this.favoritoService = favoritoService;
    }

    @PostMapping
    public ResponseEntity<ListaOutputDTO> crearLista(@RequestBody ListaInputDTO input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listaService.crearLista(input));
    }

    @GetMapping
    public ResponseEntity<List<ListaOutputDTO>> listarListas() {
        return ResponseEntity.ok(listaService.listarListas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListaOutputDTO> obtenerLista(@PathVariable Long id) {
        return ResponseEntity.ok(listaService.obtenerListaPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLista(@PathVariable Long id) {
        listaService.eliminarLista(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/favoritos")
    public ResponseEntity<List<FavoritoOutputDTO>> listarFavoritosDeLista(@PathVariable Long id) {
        return ResponseEntity.ok(favoritoService.listarFavoritosPorLista(id));
    }

    @PostMapping("/{origenId}/mover-favoritos")
    public ResponseEntity<Void> moverFavoritos(
            @PathVariable Long origenId,
            @RequestBody MoverFavoritosInputDTO input) {
        
        favoritoService.moverFavoritos(origenId, input.listaDestinoId());
        
        return ResponseEntity.ok().build();
    }
}