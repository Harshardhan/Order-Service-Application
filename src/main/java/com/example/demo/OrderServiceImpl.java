package com.example.demo;

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

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    private final OrderRepository orderRepository;
    
    private final NotificationClient notificationClient;
    private final ProductClient  productClient;
    
    
    /**
	 * @param orderRepository
	 * @param notificationClient
	 * @param productClient
	 */
	public OrderServiceImpl(OrderRepository orderRepository, NotificationClient notificationClient,
			ProductClient productClient) {
		this.orderRepository = orderRepository;
		this.notificationClient = notificationClient;
		this.productClient = productClient;
	}

	@Override
	public Order createOrder(Order order) throws InValidOrderException, OrderAlreadyExistsException {

	    // Validate the order object
	    if (order == null) {
	        logger.error("Failed to create a new Order. Order object is null");
	        throw new InValidOrderException("Order cannot be null.");
	    }

	    if (order.getCustomerId() == null || order.getPrice() == null || order.getQuantity() <= 0) {
	        logger.error("Invalid order details. Order: {}", order);
	        throw new InValidOrderException("Order details are invalid.");
	    }

	    // Check if order already exists
	    if (orderRepository.findByOrderReferenceIgnoreCase(order.getOrderReference()).isPresent()) {
	        logger.error("Order already exists with reference: {}", order.getOrderReference());
	        throw new OrderAlreadyExistsException("Order with reference " + order.getOrderReference() + " already exists.");
	    }

	    // Set order details
	    order.setOrderStatus(OrderStatus.CONFIRMED);
	    order.setOrderReference(UUID.randomUUID().toString());

	    // Save the order to the database
	    Order savedOrder = orderRepository.save(order);
	    logger.debug("Saving new order: {}", order);
	    logger.info("Order placed successfully for productId {} with orderReference {}", order.getProductId(), savedOrder.getOrderReference());

	    // Send notification
	    try {
	        // Prepare notification details
	        Notification notification = new Notification();
	        notification.setCustomerId(order.getCustomerId());
	        notification.setEmail(savedOrder.getEmail());
	        notification.setOrderId(savedOrder.getId());
	        notification.setOrderReference(savedOrder.getOrderReference());
	        notification.setMessage("Order placed successfully. Ref: " + savedOrder.getOrderReference());
	        notification.setType(NotificationType.EMAIL);
	        

	        // Send notification
	        Notification response = notificationClient.sendNotification(notification);
	        logger.info("Notification Response: {}", response.getMessage());

	    } catch (Exception e) {
	        logger.warn("Failed to send notification: {}", e.getMessage());
	        // Optionally: Retry or queue the notification if necessary
	    }

	    // Return the saved order
	    return savedOrder;
	}
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
        return orderRepository.findByOrderReferenceIgnoreCase(orderReference)
                .orElseThrow(() -> {
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
        logger.error("Fallback triggered for findByCustomerId with customerId {} due to {}", customerId, t.getMessage());
        return Collections.emptyList();
    }
    @Override
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        logger.info("Successfully retrieved {} orders", orders.size());
        return orders;
    }


	@Override
	public Order placeOrder(Long productId) {
        // Fetch product details from product service
        Product product = productClient.getProductById(productId);

        // Create an order using the fetched product details
        Order order = new Order();
        order.setProductId(product.getId());
        order.setProductName(product.getProductName());
        
        // Save or process the order further (this can involve your order repository)

        return order;
	}
}
