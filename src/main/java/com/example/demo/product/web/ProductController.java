package com.example.demo.product.web;

import com.example.demo.product.dto.ProductDTOs;
import com.example.demo.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "List products by business with pagination and optional query filter")
    public Page<ProductDTOs.ProductSummaryDTO> list(@RequestParam Long businessId,
                                                    @RequestParam(required = false) String q,
                                                    Pageable pageable) {
        return productService.list(businessId, q, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id for a business")
    public ProductDTOs.ProductDTO get(@RequestParam Long businessId, @PathVariable Long id) {
        return productService.get(businessId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product and initialize its inventory")
    public ProductDTOs.ProductDTO create(@Valid @RequestBody ProductDTOs.ProductCreateDTO dto) {
        return productService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ProductDTOs.ProductDTO update(@RequestParam Long businessId,
                                         @PathVariable Long id,
                                         @Valid @RequestBody ProductDTOs.ProductUpdateDTO dto) {
        return productService.update(businessId, id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete a product")
    public void delete(@RequestParam Long businessId, @PathVariable Long id) {
        productService.softDelete(businessId, id);
    }
}
