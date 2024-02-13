package com.order.svc.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.order.svc.dto.OrderDto;
import com.order.svc.entity.Order;
import reactor.core.publisher.Mono;

public interface OrderService {
	Mono<Order> createOrder(OrderDto orderDto);
	void reverseOrder(String event) throws JsonProcessingException;
}
