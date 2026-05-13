package com.example.demo.order.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AddressConverter implements AttributeConverter<AddressDTO, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(AddressDTO address) {
        if (address == null) return null;
        try {
            return MAPPER.writeValueAsString(address);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize address", e);
        }
    }

    @Override
    public AddressDTO convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            return MAPPER.readValue(json, AddressDTO.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not deserialize address", e);
        }
    }
}
