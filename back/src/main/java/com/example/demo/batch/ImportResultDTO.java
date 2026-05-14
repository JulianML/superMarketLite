package com.example.demo.batch;

public record ImportResultDTO(
        Long jobExecutionId,
        String status,
        long readCount,
        long writeCount,
        long skipCount
) {}
