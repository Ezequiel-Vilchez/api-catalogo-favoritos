package com.example.demo.controller;

import com.example.demo.services.FavoritoService;
import com.example.demo.services.dto.FavoritoInputDTO;
import com.example.demo.services.dto.FavoritoOutputDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Gestión de Favoritos", description = "Endpoints para administrar la lista de favoritos locales")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) 
    @Operation(summary = "Guardar favorito", description = "Valida y guarda un producto en la lista de favoritos")
    public FavoritoOutputDTO crearFavorito(@Valid @RequestBody FavoritoInputDTO input) {
        return favoritoService.crearFavorito(input);
    }

    @GetMapping
    @Operation(summary = "Listar favoritos", description = "Devuelve todos los productos guardados")
    public List<FavoritoOutputDTO> listarFavoritos() {
        return favoritoService.listarFavoritos();
    }
}