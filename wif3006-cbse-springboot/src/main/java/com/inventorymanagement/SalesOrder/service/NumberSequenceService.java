package com.inventorymanagement.SalesOrder.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class NumberSequenceService {
    private final AtomicLong counter = new AtomicLong(0);

    public String generateSoNumber() {
        long next = counter.incrementAndGet();
        return "SO" + String.format("%06d", next);
    }
}
