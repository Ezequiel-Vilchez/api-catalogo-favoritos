package com.example.demo.services;

import com.example.demo.services.dto.ProductoDTO;
import com.example.demo.services.dto.external.DummyCatalogResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Service
public class ProductoService {

    private final RestClient restClient;

    public ProductoService() {
        this.restClient = RestClient.create("https://dummyjson.com");
    }


    public List<ProductoDTO> obtenerProductos() {
        

        DummyCatalogResponse respuesta = restClient.get()
                .uri("/products")
                .retrieve()
                .body(DummyCatalogResponse.class);

        if (respuesta != null && respuesta.products() != null) {
            return respuesta.products();
        }

        return Collections.emptyList(); 
    }
}
