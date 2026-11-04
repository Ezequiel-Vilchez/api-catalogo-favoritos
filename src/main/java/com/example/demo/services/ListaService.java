package com.example.demo.services;

import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.model.Lista;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.services.dto.ListaInputDTO;
import com.example.demo.services.dto.ListaOutputDTO;
import com.example.demo.services.dto.MoverFavoritosInputDTO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;

    public ListaService(ListaRepository listaRepository, FavoritoRepository favoritoRepository) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
    }

    private Usuario getUsuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario;
        }
        throw new AccessDeniedException("Usuario no autenticado");
    }

    private void validarPropiedad(Lista lista) {
        Usuario autenticado = getUsuarioAutenticado();
        if (autenticado.rol() == Rol.ADMIN) {
            return;
        }
        if (lista.usuario() == null || !lista.usuario().id().equals(autenticado.id())) {
            throw new AccessDeniedException("No tenés permiso para acceder o modificar esta lista.");
        }
    }

    @Transactional
    public ListaOutputDTO crearLista(ListaInputDTO dto) {
        Usuario autenticado = getUsuarioAutenticado();
        Lista nuevaLista = new Lista(null, dto.nombre(), List.of(), autenticado);
        Lista guardada = listaRepository.guardar(nuevaLista);
        return ListaOutputDTO.desdeDominio(guardada);
    }

    @Transactional(readOnly = true)
    public List<ListaOutputDTO> listarListas() {
        Usuario autenticado = getUsuarioAutenticado();
        List<Lista> listas = (autenticado.rol() == Rol.ADMIN)
                ? listaRepository.obtenerTodas()
                : listaRepository.obtenerPorUsuarioId(autenticado.id());

        return listas.stream().map(ListaOutputDTO::desdeDominio).toList();
    }

    @Transactional(readOnly = true)
    public List<ListaOutputDTO> obtenerTodas() {
        return listarListas();
    }

    @Transactional(readOnly = true)
    public ListaOutputDTO obtenerListaPorId(Long id) {
        Lista lista = listaRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La lista con ID " + id + " no existe."));
        validarPropiedad(lista);
        return ListaOutputDTO.desdeDominio(lista);
    }

    @Transactional(readOnly = true)
    public ListaOutputDTO obtenerPorId(Long id) {
        return obtenerListaPorId(id);
    }

    @Transactional
    public void eliminarLista(Long id) {
        Lista lista = listaRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La lista con ID " + id + " no existe."));
        validarPropiedad(lista);
        listaRepository.eliminarPorId(id);
    }

    @Transactional
    public void eliminarPorId(Long id) {
        eliminarLista(id);
    }

    @Transactional
    public void moverFavoritos(Long origenId, MoverFavoritosInputDTO dto) {
        Lista origen = listaRepository.buscarPorId(origenId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La lista origen no existe"));
        Lista destino = listaRepository.buscarPorId(dto.listaDestinoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La lista destino no existe"));

        validarPropiedad(origen);
        validarPropiedad(destino);

        favoritoRepository.moverFavoritos(origenId, dto.listaDestinoId());
    }
}