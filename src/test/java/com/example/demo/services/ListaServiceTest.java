package com.example.demo.services;

import com.example.demo.model.Lista;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.services.dto.ListaInputDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListaServiceTest {

    @Mock
    private ListaRepository listaRepository;

    @Mock
    private FavoritoRepository favoritoRepository;

    private ListaService listaService;

    private final Usuario usuarioComun = new Usuario(1L, "user@example.com", "hash", Rol.USER);
    private final Usuario otroUsuario = new Usuario(2L, "otro@example.com", "hash", Rol.USER);
    private final Usuario admin = new Usuario(3L, "admin@example.com", "hash", Rol.ADMIN);

    @BeforeEach
    void setUp() {
        listaService = new ListaService(listaRepository, favoritoRepository);
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(
                usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol().name())));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void crearLista_usuarioAutenticado_quedaAsociadaAlUsuario() {
        autenticarComo(usuarioComun);
        when(listaRepository.guardar(any(Lista.class))).thenAnswer(inv -> inv.getArgument(0));

        listaService.crearLista(new ListaInputDTO("Peliculas"));

        ArgumentCaptor<Lista> captor = ArgumentCaptor.forClass(Lista.class);
        verify(listaRepository).guardar(captor.capture());
        assertEquals(usuarioComun.id(), captor.getValue().usuario().id());
        assertEquals("Otra cosa", captor.getValue().nombre());
    }

    @Test
    void obtenerListaPorId_listaAjena_lanzaAccesoDenegado() {
        autenticarComo(usuarioComun);
        Lista ajena = new Lista(10L, "Ajena", List.of(), otroUsuario);
        when(listaRepository.buscarPorId(10L)).thenReturn(Optional.of(ajena));

        assertThrows(AccessDeniedException.class, () -> listaService.obtenerListaPorId(10L));
    }

    @Test
    void obtenerListaPorId_adminSobreListaAjena_noLanzaExcepcion() {
        autenticarComo(admin);
        Lista ajena = new Lista(10L, "Ajena", List.of(), otroUsuario);
        when(listaRepository.buscarPorId(10L)).thenReturn(Optional.of(ajena));

        assertDoesNotThrow(() -> listaService.obtenerListaPorId(10L));
    }
}