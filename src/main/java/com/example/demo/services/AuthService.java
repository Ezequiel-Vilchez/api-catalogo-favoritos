package com.example.demo.services;

import com.example.demo.config.JwtProvider;
import com.example.demo.exception.ConflictoException;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.services.dto.LoginInputDTO;
import com.example.demo.services.dto.LoginOutputDTO;
import com.example.demo.services.dto.RegistroInputDTO;
import com.example.demo.services.dto.UsuarioOutputDTO;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public UsuarioOutputDTO registrar(RegistroInputDTO input) {
        if (usuarioRepository.existePorEmail(input.email())) {
            throw new ConflictoException("El email ya se encuentra registrado.");
        }

        String hash = passwordEncoder.encode(input.password());
        Usuario nuevoUsuario = new Usuario(null, input.email(), hash, Rol.USER);
        Usuario guardado = usuarioRepository.guardar(nuevoUsuario);

        return new UsuarioOutputDTO(guardado.id(), guardado.email(), guardado.rol());
    }

    public LoginOutputDTO login(LoginInputDTO input) {
        Usuario usuario = usuarioRepository.buscarPorEmail(input.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(input.password(), usuario.passwordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String token = jwtProvider.generarToken(usuario);
        return new LoginOutputDTO(token);
    }
}