package com.example.demo.services.dto.external;

import com.example.demo.services.dto.ProductoDTO;
import java.util.List;

public record DummyCatalogResponse(
    List<ProductoDTO> products
) {}
