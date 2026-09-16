package com.example.demo.controller;

import com.example.demo.services.ProductoService;
import com.example.demo.services.dto.ProductoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Catalogo de productos", description = "Endpoints para consumir productos externos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Obtener productos", description = "Retorna la lista de productos disponibles")
    public List<ProductoDTO> obtenerProductos() {
        return productoService.obtenerProductos();
    }
}
