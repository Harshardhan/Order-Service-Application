package com.example.demo;

import org.springframework.http.HttpHeaders; // ✅ Correct
import java.util.Collections;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.demo.excpetions.InValidOrderException;
import com.example.demo.excpetions.OrderAlreadyExistsException;
import com.example.demo.excpetions.OrderNotFoundException;
import com.example.demo.excpetions.UnauthorizedOrderAccessException;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

	private final OrderRepository orderRepository;
	private final OrderEventPublisher orderEventPublisher;
	private final NotificationClient notificationClient;
	private final ProductClient productClient;

	@Autowired
	private RestTemplate restTemplate; // For communicating with ConsolidationService

	private static final String CONSOLIDATION_SERVICE_URL = "http://CONSOLIDATION-SERVICE";

	public OrderServiceImpl(OrderRepository orderRepository, OrderEventPublisher orderEventPublisher,
			NotificationClient notificationClient, ProductClient productClient) {
		this.orderRepository = orderRepository;
		this.orderEventPublisher = orderEventPublisher;
		this.notificationClient = notificationClient;
		this.productClient = productClient;
	}

	@Override
	public Order placeOrder(Order order) throws InValidOrderException, OrderAlreadyExistsException {

		// Order validation and saving logic
		if (order == null || order.getCustomerId() == null || order.getPrice() == null || order.getQuantity() <= 0) {
			throw new InValidOrderException("Invalid order details.");
		}

		if (orderRepository.findByOrderReferenceIgnoreCase(order.getOrderReference()).isPresent()) {
			throw new OrderAlreadyExistsException("Order already exists.");
		}

		// Set order status and save
		order.setOrderStatus(OrderStatus.PLACED);
		order.setOrderReference(UUID.randomUUID().toString());
		Order savedOrder = orderRepository.save(order);
		logger.info("Order placed successfully: {}", savedOrder.getOrderReference());

		// Send notification logic
		try {
			NotificationRequest notification = new NotificationRequest();
			notification.setOrderReference(savedOrder.getOrderReference());
			notification.setEmail(savedOrder.getEmail());
			notification.setMessage("Order placed successfully.");
			notificationClient.sendNotification(notification);
		} catch (Exception e) {
			logger.warn("Failed to send notification: {}", e.getMessage());
		}

		// Communicate with Consolidation Service
		try {
			String optimizeUrl = CONSOLIDATION_SERVICE_URL + "/api/consolidations/optimize";

			Consolidation requestBody = new Consolidation();
			requestBody.setOrderReference(savedOrder.getOrderReference());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<Consolidation> requestEntity = new HttpEntity<>(requestBody, headers);
			logger.info("Calling Consolidation Service at: {}", optimizeUrl);

			ResponseEntity<Consolidation> response = restTemplate.postForEntity(optimizeUrl, requestEntity,
					Consolidation.class);
			logger.info("Consolidation Service response status: {}", response.getStatusCode());

			Consolidation consolidation = response.getBody();

			if (consolidation != null) {
				logger.info("Successfully triggered consolidation for order: {}", consolidation.getOrderReference());
			} else {
				logger.warn("Consolidation service returned null for order: {}", savedOrder.getOrderReference());
			}

		} catch (Exception e) {
			logger.error("Failed to communicate with Consolidation Service for order {}: {}",
					savedOrder.getOrderReference(), e.getMessage());
		}

		// Publish event to RabbitMQ
		orderEventPublisher.sendOrderEvent(savedOrder);

		// ✅ Return saved order (fix)
		return savedOrder;
	}
	// Other methods like updateOrder(), deleteOrder(), etc., remain the same

	@Override
	public List<Order> processOrder(Long orderId) {
		logger.warn("processOrder is not yet implemented for orderId: {}", orderId);
		return Collections.emptyList(); // Instead of throwing an exception
	}

	@Override
	public Order updateOrder(Long orderId, Order updatedOrder) throws OrderNotFoundException {
		// Step 1: Fetch the existing order
		Order existingOrder = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

		// Step 2: Update only non-null fields
		if (updatedOrder.getDescription() != null) {
			existingOrder.setDescription(updatedOrder.getDescription());
		}
		if (updatedOrder.getQuantity() > 0) {
			existingOrder.setQuantity(updatedOrder.getQuantity());
		}
		if (updatedOrder.getPrice() != null) {
			existingOrder.setPrice(updatedOrder.getPrice());
		}
		if (updatedOrder.getOrderType() != null) {
			existingOrder.setOrderType(updatedOrder.getOrderType());
		}
		if (updatedOrder.getPaymentMethod() != null) {
			existingOrder.setPaymentMethod(updatedOrder.getPaymentMethod());
		}
		if (updatedOrder.getAddress() != null) {
			existingOrder.setAddress(updatedOrder.getAddress());
		}
		if (updatedOrder.getOrderStatus() != null) {
			existingOrder.setOrderStatus(updatedOrder.getOrderStatus());
		}

		// Step 3: Save the updated order in the database
		Order savedOrder = orderRepository.save(existingOrder);

		// Logging
		logger.info("Order with ID {} updated successfully", orderId);

		return savedOrder;
	}

	@Override
	public void deleteOrder(Long orderId) throws OrderNotFoundException {
		Optional<Order> order = orderRepository.findById(orderId);
		if (order.isEmpty()) {
			logger.error("Attempted to delete non-existent order with id {}", orderId);
			throw new OrderNotFoundException("Order with id " + orderId + " not found.");
		}

		orderRepository.deleteById(orderId);
		logger.info("Successfully deleted order: {}", order.get());
	}

	@Override
	public List<Order> getOrdersForCustomer(Long customerId) throws UnauthorizedOrderAccessException {
		logger.warn("getOrdersForCustomer is not yet implemented.");
		throw new UnauthorizedOrderAccessException("getOrdersForCustomer is not yet implemented.");
	}

	@Override
	public Order findByOrderReference(String orderReference) throws OrderNotFoundException {
		return orderRepository.findByOrderReferenceIgnoreCase(orderReference).orElseThrow(() -> {
			logger.error("Order not found for reference: {}", orderReference);
			return new OrderNotFoundException("Order with reference " + orderReference + " not found.");
		});
	}

	@Override
	@CircuitBreaker(name = "OrderService", fallbackMethod = "getOrderFallback")
	@Retry(name = "OrderService")
	@RateLimiter(name = "OrderService")
	public List<Order> findByCustomerId(Long customerId) throws OrderNotFoundException {
		List<Order> orders = orderRepository.findByCustomerId(customerId);
		if (orders.isEmpty()) {
			logger.warn("No orders found for customerId {}", customerId);
			throw new OrderNotFoundException("No orders found for customerId " + customerId);
		}
		return orders;
	}

	public List<Order> getOrderFallback(Long customerId, Throwable t) {
		logger.error("Fallback triggered for findByCustomerId with customerId {} due to {}", customerId,
				t.getMessage());
		return Collections.emptyList();
	}

	@Override
	public List<Order> getAllOrders() {
		List<Order> orders = orderRepository.findAll();
		logger.info("Successfully retrieved {} orders", orders.size());
		return orders;
	}

	@Override
	public Order placeOrder(Long productId) throws InValidOrderException, OrderAlreadyExistsException {
		Product product = productClient.getProductById(productId);

		if (product == null) {
			throw new RuntimeException("Product not found with ID: " + productId);
		}

		Order order = new Order();
		order.setProductId(productId);
		order.setProductName(product.getProductName());
		order.setPrice(product.getPrice());
		order.setQuantity(1); // or get from user input
		order.setCustomerId(1L); // or pass from user context or input
		order.setOrderType("Online");
		order.setPaymentMethod("Cash on Delivery");
		order.setAddress("Default Address"); // Optional
		order.setEmail("example@example.com");

		return placeOrder(order);
	}
}
