package com.example.demo;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;


public class LoggingException {
    private static final Logger logger = LoggerFactory.getLogger(LoggingException.class);

    // Method that throws an exception
    private static int divide(int a, int b) {
        return a / b;  // This will cause ArithmeticException if b = 0
    }

 public static void main(String[] args) {
	 try {
		 int result = divide(10,2);
		 logger.info("User {} attempted to divide {} by {}", "Harsha", 10, 2);

		 System.out.println("Result: "+result);
	 }catch(ArithmeticException e) {
		 logger.error("Exception occured: {}", e.getMessage(), e);
	 }
	
}
}
