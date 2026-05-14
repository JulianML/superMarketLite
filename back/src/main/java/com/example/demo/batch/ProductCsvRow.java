package com.example.demo.batch;

import lombok.Data;

@Data
public class ProductCsvRow {
    private String sku;
    private String name;
    private String description;
    private String price;
    private String currency;
    private String vatRate;
    private String category;
    private String imageUrl;
    private String stock;
    private String safetyStock;
}
