package com.example.demo.batch;

import com.example.demo.product.repo.ProductRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ProductCsvImportConfig {

    @Bean
    public Job productCsvImportJob(JobRepository jobRepository, Step productCsvImportStep) {
        return new JobBuilder("productCsvImportJob", jobRepository)
                .start(productCsvImportStep)
                .build();
    }

    @Bean
    public Step productCsvImportStep(JobRepository jobRepository,
                                     PlatformTransactionManager txManager,
                                     FlatFileItemReader<ProductCsvRow> productCsvReader,
                                     ProductCsvItemProcessor productCsvItemProcessor,
                                     ProductCsvItemWriter writer) {
        return new StepBuilder("productCsvImportStep", jobRepository)
                .<ProductCsvRow, ProductImportItem>chunk(20, txManager)
                .reader(productCsvReader)
                .processor(productCsvItemProcessor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(50)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<ProductCsvRow> productCsvReader(
            @Value("#{jobParameters['filePath']}") String filePath) {
        return new FlatFileItemReaderBuilder<ProductCsvRow>()
                .name("productCsvReader")
                .resource(new FileSystemResource(filePath))
                .delimited()
                .delimiter(",")
                .quoteCharacter('"')
                .names("sku", "name", "description", "price", "currency",
                       "vatRate", "category", "imageUrl", "stock", "safetyStock")
                .linesToSkip(1)
                .targetType(ProductCsvRow.class)
                .build();
    }

    @Bean
    @StepScope
    public ProductCsvItemProcessor productCsvItemProcessor(
            @Value("#{jobParameters['businessId']}") Long businessId,
            ProductRepository productRepository) {
        return new ProductCsvItemProcessor(productRepository, businessId);
    }
}
