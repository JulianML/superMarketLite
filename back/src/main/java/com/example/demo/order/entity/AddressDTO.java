package com.example.demo.order.entity;

public record AddressDTO(
        String street,
        String city,
        String postalCode,
        String country
) {}
