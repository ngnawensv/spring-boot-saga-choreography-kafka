package com.order.svc.controller;

import com.order.svc.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.order.svc.dto.OrderDto;
import com.order.svc.entity.Order;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class OrderController {

	private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
	public ResponseEntity<Mono<Order>> createOrder(@RequestBody OrderDto orderDto) {
		log.info("OrderController:createOrder:orderDto {} ", orderDto);
		var result = orderService.createOrder(orderDto);
		return ResponseEntity.ok().body(result);

	}
}
