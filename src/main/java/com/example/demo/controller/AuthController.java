package com.example.demo.controller;

import com.example.demo.services.AuthService;
import com.example.demo.services.dto.LoginInputDTO;
import com.example.demo.services.dto.LoginOutputDTO;
import com.example.demo.services.dto.RegistroInputDTO;
import com.example.demo.services.dto.UsuarioOutputDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro y login de usuarios")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo usuario")
    public ResponseEntity<UsuarioOutputDTO> registrar(@Valid @RequestBody RegistroInputDTO input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(input));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener token JWT")
    public ResponseEntity<LoginOutputDTO> login(@Valid @RequestBody LoginInputDTO input) {
        return ResponseEntity.ok(authService.login(input));
    }
}