package com.example.demo.batch;

import com.example.demo.product.entity.Product;
import com.example.demo.product.repo.ProductRepository;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

public class ProductCsvItemProcessor implements ItemProcessor<ProductCsvRow, ProductImportItem> {

    private final ProductRepository productRepository;
    private final Long businessId;

    public ProductCsvItemProcessor(ProductRepository productRepository, Long businessId) {
        this.productRepository = productRepository;
        this.businessId = businessId;
    }

    @Override
    public ProductImportItem process(ProductCsvRow row) {
        if (row.getSku() == null || row.getSku().isBlank()) return null;

        // Skip if SKU already exists for this business
        if (productRepository.findByBusinessIdAndSku(businessId, row.getSku()).isPresent()) {
            return null;
        }

        Product product = new Product();
        product.setBusinessId(businessId);
        product.setSku(row.getSku().trim());
        product.setName(row.getName().trim());
        product.setDescription(row.getDescription() != null ? row.getDescription().trim() : null);
        product.setPrice(new BigDecimal(row.getPrice().trim()));
        product.setCurrency(row.getCurrency() != null && !row.getCurrency().isBlank()
                ? row.getCurrency().trim() : "EUR");
        product.setVatRate(row.getVatRate() != null && !row.getVatRate().isBlank()
                ? new BigDecimal(row.getVatRate().trim()) : null);

        int stock = parseIntOrDefault(row.getStock(), 0);
        int safetyStock = parseIntOrDefault(row.getSafetyStock(), 0);

        return new ProductImportItem(product, stock, safetyStock, row.getImageUrl());
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return (value != null && !value.isBlank()) ? Integer.parseInt(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
