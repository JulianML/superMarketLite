package com.example.demo.common.web;

import com.example.demo.common.exception.BusinessRuleException;
import com.example.demo.common.exception.DuplicateSkuException;
import com.example.demo.common.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler({BusinessRuleException.class, DuplicateSkuException.class})
    public ResponseEntity<?> handleBusiness(Exception ex) {
        HttpStatus status = ex instanceof DuplicateSkuException ? HttpStatus.CONFLICT : HttpStatus.UNPROCESSABLE_ENTITY;
        String code = ex instanceof DuplicateSkuException ? "DUPLICATE_SKU" : "BUSINESS_RULE";
        return build(status, code, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.put(fe.getField(), fe.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", details);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatus(ResponseStatusException ex) {
        return build((HttpStatus) ex.getStatusCode(), "AUTH_ERROR", ex.getReason(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage(), null);
    }

    private ResponseEntity<?> build(HttpStatus status, String code, String message, Map<String, ?> details) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("code", code);
        body.put("message", message);
        if (details != null) body.put("details", details);
        body.put("traceId", UUID.randomUUID().toString());
        return new ResponseEntity<>(body, status);
    }
}
