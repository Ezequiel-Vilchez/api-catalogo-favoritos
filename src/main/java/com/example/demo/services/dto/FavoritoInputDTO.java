package com.example.demo.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FavoritoInputDTO(
    @NotNull(message = "El ID del producto es obligatorio")
    Long productoId,

    @NotBlank(message = "La nota personal no puede estar vacía")
    @Size(max = 255, message = "La nota no puede superar los 255 caracteres")
    String notaPersonal
) {}
