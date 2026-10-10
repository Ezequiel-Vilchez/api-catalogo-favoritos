package com.example.demo.services;

import com.example.demo.model.Lista;
import com.example.demo.repository.ListaRepository;
import com.example.demo.services.dto.ListaInputDTO;
import com.example.demo.services.dto.ListaOutputDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListaService {

    private final ListaRepository listaRepository;

    public ListaService(ListaRepository listaRepository) {
        this.listaRepository = listaRepository;
    }

    public ListaOutputDTO crearLista(ListaInputDTO input) {
        Lista nuevaLista = new Lista(null, input.nombre());
        Lista guardada = listaRepository.guardar(nuevaLista);
        return convertirAOutputDTO(guardada);
    }

    public List<ListaOutputDTO> listarListas() {
        return listaRepository.buscarTodas().stream()
                .map(this::convertirAOutputDTO)
                .collect(Collectors.toList());
    }

    public ListaOutputDTO obtenerListaPorId(Long id) {
        Lista lista = listaRepository.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Lista no encontrada"));
        return convertirAOutputDTO(lista);
    }

    public void eliminarLista(Long id) {
        obtenerListaPorId(id);
        listaRepository.eliminar(id);
    }

    private ListaOutputDTO convertirAOutputDTO(Lista lista) {
        return new ListaOutputDTO(lista.id(), lista.nombre());
    }
}