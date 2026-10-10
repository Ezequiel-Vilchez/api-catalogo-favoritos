package com.example.demo.services.dto;

import jakarta.validation.constraints.NotNull;

public record MoverFavoritosInputDTO(
    @NotNull(message = "El ID de la lista de destino es obligatorio")
    Long listaDestinoId
) {}
