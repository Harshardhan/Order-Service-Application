package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductFallback implements ProductClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductFallback.class);

	@Override
	public Product getProductById(Long id) {
        logger.warn("Product service is unavailable. Returning fallback product for ID: {}", id);

        Product fallbackProduct = new Product();
        fallbackProduct.setId(id);
        fallbackProduct.setProductName("Fallback Product");
        fallbackProduct.setDescription("Product service is unavailable");
        fallbackProduct.setPrice(null);
        fallbackProduct.setCategory(null);
        fallbackProduct.setId(id);
        return fallbackProduct;

}
}