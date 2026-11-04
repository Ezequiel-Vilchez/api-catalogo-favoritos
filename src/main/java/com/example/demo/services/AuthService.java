package com.example.demo.services;

import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.services.dto.RegistroInputDTO;
import com.example.demo.services.dto.UsuarioOutputDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioOutputDTO registrar(RegistroInputDTO input) {
        if (usuarioRepository.existePorEmail(input.email())) {
            throw new DataIntegrityViolationException("El email ya se encuentra registrado.");
        }

        String hash = passwordEncoder.encode(input.password());
        Usuario nuevoUsuario = new Usuario(null, input.email(), hash, Rol.USER);
        Usuario guardado = usuarioRepository.guardar(nuevoUsuario);

        return new UsuarioOutputDTO(guardado.id(), guardado.email(), guardado.rol());
    }
}
