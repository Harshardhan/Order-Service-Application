package com.example.demo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{
	
	//find all orders by  a specific customer
	public List<Order> findByCustomerId(Long customerId);

	//find orders by status
	List<Order> findByOrderStatus(OrderStatus orderStatus); // ✅ Corrected property name
	
    // ✅ Find orders containing a specific keyword in the description
	public List<Order> findByDescriptionContaining(String keyword);
	
    // ✅ Find orders by price greater than a specific value
    List<Order> findByPriceGreaterThan(BigDecimal price);

    // ✅ Custom Query: Find orders created in the last X days
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :fromDate")
    List<Order> findRecentOrders(@Param("fromDate") LocalDateTime fromDate);

    // ✅ Custom Query: Find by order reference (case-insensitive)
    Optional<Order> findByOrderReferenceIgnoreCase(String orderReference);

}
