package com.example.demo.services.dto;

import java.time.LocalDateTime;

public record FavoritoOutputDTO(
    Long id,
    Long productoId,
    String notaPersonal,
    LocalDateTime fechaAgregado
){}
