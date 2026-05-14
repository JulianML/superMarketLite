package com.example.demo.batch;

import com.example.demo.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductImportItem {
    private final Product product;
    private final int stock;
    private final int safetyStock;
    private final String imageUrl;
}
