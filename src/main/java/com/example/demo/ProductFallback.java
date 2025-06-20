package com.example.demo;

import org.springframework.stereotype.Component;

@Component
public class ProductFallback implements ProductClient {
    @Override
    public Product getProductById(Long id) {
        Product fallbackProduct = new Product();
        fallbackProduct.setId(id);
        fallbackProduct.setProductName("Fallback Product");
        fallbackProduct.setDescription("Product service is currently unavailable.");
        fallbackProduct.setCategory("Fallback Category");
        return fallbackProduct;
    }
}
