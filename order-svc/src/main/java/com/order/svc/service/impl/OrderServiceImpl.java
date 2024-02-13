package com.order.svc.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.svc.dto.OrderDto;
import com.order.svc.dto.OrderEvent;
import com.order.svc.entity.Order;
import com.order.svc.repository.OrderRepository;
import com.order.svc.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {


	private final  OrderRepository repository;

	private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
	private final ObjectMapper objectMapper;

    public OrderServiceImpl(OrderRepository repository, KafkaTemplate<String, OrderEvent> kafkaTemplate, ObjectMapper objectMapper) {
		this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

	@Override
	public Mono<Order> createOrder(OrderDto orderDto) {
		log.info("OrderServiceImpl:createOrder orderDto {} ",orderDto);
		var order = Order.builder()
				.amount(orderDto.getAmount())
				.item(orderDto.getItem())
				.quantity(orderDto.getQuantity())
				.status("CREATED")
				.build();
		log.info("OrderServiceImpl:createOrder order {} ",order);
		return repository.save(order)
				.doOnSuccess(savedOrder-> {
					orderDto.setOrderId(savedOrder.getId());
					var event =OrderEvent.builder()
							.order(orderDto)
							.type("ORDER_CREATED")
							.build();
					log.info("OrderServiceImpl:createOrder event {} ",event);
					kafkaTemplate.send("new-order", event);
				})
				.doOnError(throwable -> {
					order.setStatus("FAILED");
					repository.save(order);
					log.info("OrderServiceImpl:createOrder order save with status fail {} ",order);
				});
				//.onErrorMap(throwable -> new Exception(String.format("Error occurred when creating order.Cause %s", throwable)));
	}

	@KafkaListener(topics = "reversed-order", groupId = "order-group")
	@Override
	public void reverseOrder(String event) throws JsonProcessingException {
		log.info("OrderServiceImpl:reverseOrder event {} ",event);

		OrderEvent orderEvent = objectMapper.readValue(event, OrderEvent.class);
		var orderId = orderEvent.getOrder().getOrderId();

		repository.findById(orderId)
				.flatMap(order -> {
					order.setStatus("FAILED");
					log.info("OrderServiceImpl:reverseOrder order reversed successfully {} ",order);
					return repository.save(order);
				})
				.switchIfEmpty(Mono.error(new Exception(String.format("Order not found with orderId %s", orderId))))
				.onErrorMap(throwable -> {
					// Transforming any error into a custom exception
					log.error("Failed to reverse order ");
					return new Exception(String.format("Failed to to reverse order. Cause %s", throwable));
				});
	}
}
