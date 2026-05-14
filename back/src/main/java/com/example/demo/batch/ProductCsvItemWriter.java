package com.example.demo.batch;

import com.example.demo.inventory.entity.Inventory;
import com.example.demo.inventory.repo.InventoryRepository;
import com.example.demo.product.entity.Product;
import com.example.demo.product.entity.ProductImage;
import com.example.demo.product.repo.ProductImageRepository;
import com.example.demo.product.service.ProductService;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProductCsvItemWriter implements ItemWriter<ProductImportItem> {

    private final ProductService productService;
    private final InventoryRepository inventoryRepository;
    private final ProductImageRepository productImageRepository;

    public ProductCsvItemWriter(ProductService productService,
                                InventoryRepository inventoryRepository,
                                ProductImageRepository productImageRepository) {
        this.productService = productService;
        this.inventoryRepository = inventoryRepository;
        this.productImageRepository = productImageRepository;
    }

    @Override
    public void write(Chunk<? extends ProductImportItem> chunk) {
        for (ProductImportItem item : chunk) {
            Product saved = productService.createProductFromCSV(item.getProduct());

            Inventory inventory = new Inventory();
            inventory.setBusinessId(saved.getBusinessId());
            inventory.setProductId(saved.getId());
            inventory.setStock(item.getStock());
            inventory.setSafetyStock(item.getSafetyStock());
            inventoryRepository.save(inventory);

            String imageUrl = item.getImageUrl();
            if (imageUrl != null && !imageUrl.isBlank()) {
                ProductImage image = new ProductImage();
                image.setProductId(saved.getId());
                image.setUrl(imageUrl.trim());
                image.setPosition(0);
                image.setAltText(saved.getName());
                image.setUpdatedAt(LocalDateTime.now());
                productImageRepository.save(image);
            }
        }
    }
}
