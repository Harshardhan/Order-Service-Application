package com.example.demo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.excpetions.InValidOrderException;
import com.example.demo.excpetions.OrderAlreadyExistsException;
import com.example.demo.excpetions.OrderNotFoundException;
import com.example.demo.excpetions.OrderProcessingException;
import com.example.demo.excpetions.UnauthorizedOrderAccessException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping()
	public ResponseEntity<Order> createOrder(@RequestBody @Valid Order order)
			throws InValidOrderException, OrderAlreadyExistsException {
		Order createdOrder = orderService.placeOrder(order);
		logger.info("Order created successfully - OrderId: {}, Customer: {}", order.toString());
		return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);

	}
    @GetMapping("/{productId}")
    public ResponseEntity<Order> placeOrder(@PathVariable Long productId) throws InValidOrderException, OrderAlreadyExistsException {
        Order order = orderService.placeOrder(productId);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }


	@GetMapping("/customer/{id}")
	public ResponseEntity<List<Order>> findByCustomerId(@PathVariable("id") Long customerId)
			throws OrderNotFoundException {
		List<Order> orders = orderService.findByCustomerId(customerId);
		if (orders.isEmpty()) {
			logger.error("Failed to retrieve details: No orders found for customerId {}", customerId);
			throw new OrderNotFoundException("No orders found for customerId " + customerId);
		}
		logger.info("Successfully retrieved {} orders for customerId {}", orders.size(), customerId);

		return new ResponseEntity<>(orders, HttpStatus.OK);
	}


	@GetMapping()
	public ResponseEntity<List<Order>> getAllOrders() {
		List<Order> orders = orderService.getAllOrders();

		if (orders.isEmpty()) {
			logger.warn("No orders found in the system.");
			return ResponseEntity.noContent().build();
		}

		logger.info("Successfully retrieved {} orders", orders.size());
		return ResponseEntity.ok(orders);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Order> updateOrder(@PathVariable("id") Long orderId, @RequestBody @Valid Order updatedOrder) throws OrderNotFoundException {
	    Order updated = orderService.updateOrder(orderId, updatedOrder);  // ✅ Correct: Return single Order
	    logger.info("Successfully updated order with orderId {}", orderId);
	    return ResponseEntity.ok(updated);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long orderId) throws OrderNotFoundException {
	    orderService.deleteOrder(orderId);
	    logger.info("Order with ID {} deleted successfully", orderId);
	    return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/reference/{orderReference}")
	public ResponseEntity<Order> findByOrderReference(@PathVariable String orderReference) throws OrderNotFoundException {
	    Order referenceOrder = orderService.findByOrderReference(orderReference);
	    logger.info("Successfully retrieved details of an order with orderReference {}", orderReference);
	    return ResponseEntity.ok(referenceOrder);
	}
	@PutMapping("/{orderId}/process") // ✅ Change "id" to "orderId"
	public ResponseEntity<List<Order>> processOrder(@PathVariable("orderId") Long orderId) throws OrderProcessingException {
	    List<Order> orderProcess = orderService.processOrder(orderId);
	    logger.info("Order will be successfully processed with orderId {}", orderId);
	    return ResponseEntity.ok(orderProcess);
	}
	
	@GetMapping("/order/{id}")
	public ResponseEntity<List<Order>> getOrdersForCustomer(@PathVariable("id") Long customerId) throws UnauthorizedOrderAccessException {
	    List<Order> ordersCustomer = orderService.getOrdersForCustomer(customerId);
	    logger.info("Successfully retrieved orders for customer {}", customerId);
	    return ResponseEntity.ok(ordersCustomer);
	}
}