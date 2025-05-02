package com.example.demo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PRODUCT-SERVICE", fallback = ProductFallback.class)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    Product getProductById(@PathVariable("id") Long id);
}
