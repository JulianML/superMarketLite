package com.example.demo.product.service;

import com.example.demo.common.exception.DuplicateSkuException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.common.kafka.KafkaProducerService;
import com.example.demo.inventory.entity.Inventory;
import com.example.demo.inventory.repo.InventoryRepository;
import com.example.demo.product.dto.ProductDTOs;
import com.example.demo.product.dto.ProductEvent;
import com.example.demo.product.entity.Product;
import com.example.demo.product.repo.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final KafkaProducerService kafkaProducerService;

    public ProductService(ProductRepository productRepository, InventoryRepository inventoryRepository, KafkaProducerService kafkaProducerService) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public Page<ProductDTOs.ProductSummaryDTO> list(Long businessId, String q, Pageable pageable) {
        Page<Product> page = (q == null || q.isBlank())
                ? productRepository.findByBusinessIdAndIsActiveTrue(businessId, pageable)
                : productRepository.search(businessId, q, pageable);
        return page.map(this::toSummaryDTO);
    }

    public ProductDTOs.ProductDTO get(Long businessId, Long id) {
        Product product = productRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return toDTO(product);
    }

    @Transactional
    public ProductDTOs.ProductDTO create(ProductDTOs.ProductCreateDTO dto) {
        // Uniqueness by (businessId, sku)
        Optional<Product> existing = productRepository.findByBusinessIdAndSku(dto.businessId, dto.sku);
        if (existing.isPresent()) {
            throw new DuplicateSkuException("SKU already exists for this business");
        }
        Product p = new Product();
        p.setBusinessId(dto.businessId);
        p.setSku(dto.sku);
        p.setName(dto.name);
        p.setDescription(dto.description);
        p.setPrice(dto.price);
        p.setCurrency(dto.currency != null ? dto.currency : "EUR");
        p.setVatRate(dto.vatRate);
        p = productRepository.save(p);

        // Initialize inventory with stock 0
        Inventory inv = new Inventory();
        inv.setBusinessId(p.getBusinessId());
        inv.setProductId(p.getId());
        inv.setStock(0);
        inv.setSafetyStock(0);
        inventoryRepository.save(inv);


        sendToKafka(p);

        return toDTO(p);
    }

    private void sendToKafka(Product p) {
        // Enviar evento a Kafka
        ProductEvent event = new ProductEvent(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                "CREATE",
                "API"
        );
        kafkaProducerService.sendProductEvent(event);
    }

    public Product createProductFromCSV(Product product) {
        Product savedProduct = productRepository.save(product);

        LocalDateTime timestamp = LocalDateTime.now();
        // Enviar evento a Kafka
        ProductEvent event = new ProductEvent(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                "CREATE",
                "CSV_IMPORT"
        );
        kafkaProducerService.sendProductEvent(event);

        return savedProduct;
    }

    @Transactional
    public ProductDTOs.ProductDTO update(Long businessId, Long id, ProductDTOs.ProductUpdateDTO dto) {
        Product p = productRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        // Check SKU uniqueness if changed
        if (!p.getSku().equals(dto.sku)) {
            final Long currentId = p.getId();
            productRepository.findByBusinessIdAndSku(businessId, dto.sku).ifPresent(other -> {
                if (!other.getId().equals(currentId)) {
                    throw new DuplicateSkuException("SKU already exists for this business");
                }
            });
        }
        p.setSku(dto.sku);
        p.setName(dto.name);
        p.setDescription(dto.description);
        p.setPrice(dto.price);
        if (dto.currency != null) p.setCurrency(dto.currency);
        p.setVatRate(dto.vatRate);
        if (dto.isActive != null) p.setActive(dto.isActive);
        p = productRepository.save(p);

        sendToKafka(p);

        return toDTO(p);
    }

    @Transactional
    public void softDelete(Long businessId, Long id) {
        Product p = productRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        p.setActive(false);
        p.setDeletedAt(java.time.LocalDateTime.now());
        productRepository.save(p);
    }

    private ProductDTOs.ProductDTO toDTO(Product p) {
        ProductDTOs.ProductDTO dto = new ProductDTOs.ProductDTO();
        dto.id = p.getId();
        dto.businessId = p.getBusinessId();
        dto.sku = p.getSku();
        dto.name = p.getName();
        dto.description = p.getDescription();
        dto.price = p.getPrice();
        dto.currency = p.getCurrency();
        dto.vatRate = p.getVatRate();
        dto.isActive = p.isActive();
        dto.createdAt = p.getCreatedAt();
        dto.updatedAt = p.getUpdatedAt();
        dto.deletedAt = p.getDeletedAt();
        return dto;
    }

    private ProductDTOs.ProductSummaryDTO toSummaryDTO(Product p) {
        ProductDTOs.ProductSummaryDTO dto = new ProductDTOs.ProductSummaryDTO();
        dto.id = p.getId();
        dto.businessId = p.getBusinessId();
        dto.sku = p.getSku();
        dto.name = p.getName();
        dto.price = p.getPrice();
        dto.currency = p.getCurrency();
        dto.isActive = p.isActive();
        return dto;
    }
}
