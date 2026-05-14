package com.example.demo.product.web;

import com.example.demo.batch.ImportResultDTO;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/products")
public class ProductImportController {

    private final JobLauncher jobLauncher;
    private final Job productCsvImportJob;

    public ProductImportController(JobLauncher jobLauncher, Job productCsvImportJob) {
        this.jobLauncher = jobLauncher;
        this.productCsvImportJob = productCsvImportJob;
    }

    @PostMapping("/import/csv")
    public ResponseEntity<ImportResultDTO> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "2") Long businessId) throws Exception {

        Path tempFile = Files.createTempFile("product-import-", ".csv");
        try {
            file.transferTo(tempFile);

            JobParameters params = new JobParametersBuilder()
                    .addString("filePath", tempFile.toAbsolutePath().toString())
                    .addLong("businessId", businessId)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(productCsvImportJob, params);

            long readCount = 0, writeCount = 0, skipCount = 0;
            for (StepExecution step : execution.getStepExecutions()) {
                readCount  += step.getReadCount();
                writeCount += step.getWriteCount();
                skipCount  += step.getProcessSkipCount() + step.getWriteSkipCount();
            }

            return ResponseEntity.ok(new ImportResultDTO(
                    execution.getId(),
                    execution.getStatus().name(),
                    readCount,
                    writeCount,
                    skipCount
            ));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
