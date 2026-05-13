package com.example.demo.order.util;

import java.time.Year;
import java.util.UUID;

public final class OrderNumberGenerator {

    private OrderNumberGenerator() {}

    public static String generate() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD-" + Year.now().getValue() + "-" + uuid;
    }
}
