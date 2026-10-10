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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/listas")
@Tag(name = "Listas", description = "Operaciones relacionadas con las listas de favoritos")
public class ListaController {

    private final ListaService listaService;
    private final FavoritoService favoritoService;

    public ListaController(ListaService listaService, FavoritoService favoritoService) {
        this.listaService = listaService;
        this.favoritoService = favoritoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una nueva lista")
    public ResponseEntity<ListaOutputDTO> crearLista(@RequestBody ListaInputDTO input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listaService.crearLista(input));
    }

    @GetMapping
    @Operation(summary = "Obtener todas las listas")
    public ResponseEntity<List<ListaOutputDTO>> listarListas() {
        return ResponseEntity.ok(listaService.listarListas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una lista por su ID")
    public ResponseEntity<ListaOutputDTO> obtenerLista(@PathVariable Long id) {
        return ResponseEntity.ok(listaService.obtenerListaPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una lista (debe estar vacía)")
    public ResponseEntity<Void> eliminarLista(@PathVariable Long id) {
        listaService.eliminarLista(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/favoritos")
    @Operation(summary = "Obtener todos los favoritos de una lista específica")
    public ResponseEntity<List<FavoritoOutputDTO>> listarFavoritosDeLista(@PathVariable Long id) {
        return ResponseEntity.ok(favoritoService.listarFavoritosPorLista(id));
    }

    @PostMapping("/{origenId}/mover-favoritos")
    @Operation(summary = "Mover todos los favoritos de una lista origen a una destino de forma atómica")
    public ResponseEntity<Void> moverFavoritos(
            @PathVariable Long origenId,
            @RequestBody MoverFavoritosInputDTO input) {
        
        favoritoService.moverFavoritos(origenId, input.listaDestinoId());
        
        return ResponseEntity.ok().build();
    }
}