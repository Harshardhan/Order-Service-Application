package com.example.demo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


public interface OrderService {

	public Order createOrder(Order order)throws InValidOrderException, OrderAlreadyExistsException ;
	
	public List<Order> processOrder(Long orderId)throws OrderProcessingException;
	
	public Order updateOrder(Long orderId, Order updatedOrder)throws OrderNotFoundException;
	
	public void deleteOrder(Long orderId)throws OrderNotFoundException;
	
	public List<Order> getOrdersForCustomer(Long customerId)throws UnauthorizedOrderAccessException;
	
	public Order findByOrderReference(String orderReference)throws OrderNotFoundException;
	
	public List<Order> findByCustomerId(Long customerId) throws OrderNotFoundException;
	
	public List<Order> getAllOrders();

	public Order placeOrder(Long productId);

}
