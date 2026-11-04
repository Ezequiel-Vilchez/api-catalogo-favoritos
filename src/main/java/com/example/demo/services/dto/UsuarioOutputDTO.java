package com.example.demo.services.dto;

import com.example.demo.model.Rol;

public record UsuarioOutputDTO(
    Long id,
    String email,
    Rol rol
) {}
