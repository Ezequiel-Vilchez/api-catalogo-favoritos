package com.example.demo.services;

import com.example.demo.config.JwtProvider;
import com.example.demo.exception.ConflictoException;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.services.dto.RegistroInputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, passwordEncoder, jwtProvider);
    }

    @Test
    void registrar_passwordValida_nuncaGuardaTextoPlano() {
        when(usuarioRepository.existePorEmail("a@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secreta123")).thenReturn("$2a$hash-simulado");
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            return new Usuario(1L, u.email(), u.passwordHash(), u.rol());
        });

        authService.registrar(new RegistroInputDTO("a@example.com", "secreta123"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardar(captor.capture());
        assertNotEquals("secreta123", captor.getValue().passwordHash());
        assertEquals("$2a$hash-simulado", captor.getValue().passwordHash());
        assertEquals(Rol.USER, captor.getValue().rol());
    }

    @Test
    void registrar_emailExistente_lanzaConflictoYNoGuarda() {
        when(usuarioRepository.existePorEmail("a@example.com")).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> authService.registrar(new RegistroInputDTO("a@example.com", "secreta123")));

        verify(usuarioRepository, never()).guardar(any());
    }
}