package com.payment.svc.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.payment.svc.dto.OrderDto;
import com.payment.svc.dto.OrderEvent;
import com.payment.svc.entity.Payment;
import com.payment.svc.repository.PaymentRepository;
import com.payment.svc.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository repository;
	private final KafkaTemplate<String, OrderEvent> kafkaPaymentTemplate;
	private final KafkaTemplate<String, OrderEvent> kafkaOrderTemplate;

    public PaymentServiceImpl(PaymentRepository repository, KafkaTemplate<String, OrderEvent> kafkaPaymentTemplate, KafkaTemplate<String, OrderEvent> kafkaOrderTemplate) {
        this.repository = repository;
        this.kafkaPaymentTemplate = kafkaPaymentTemplate;
        this.kafkaOrderTemplate = kafkaOrderTemplate;
    }

	@KafkaListener(topics = "new-order", groupId = "order-group")
	@Override
	public void processPayment(OrderEvent orderEvent) {
		log.info("Received event for payment {}", orderEvent);
		OrderDto order = orderEvent.getOrder();
		Payment payment = Payment.builder()
				.amount(order.getAmount())
				.mode(order.getPaymentMode())
				.orderId(order.getOrderId())
				.status("SUCCESS")
				.build();
		 repository.save(payment)
				.doOnSuccess(paymentSaved -> {
					log.info("Payment saved {}", paymentSaved);
					OrderEvent paymentEvent = OrderEvent.builder()
							.order(orderEvent.getOrder())
							.type("PAYMENT_CREATED")
							.build();
					Message<OrderEvent> message = MessageBuilder
							.withPayload(paymentEvent)
							.setHeader(KafkaHeaders.TOPIC,"new-payment")
							.build();
					kafkaPaymentTemplate.send(message);
				})
				.doOnError(throwable -> {
					log.info("Payment reversed {}", payment);
					payment.setOrderId(order.getOrderId());
					payment.setStatus("FAILED");
					repository.save(payment);

					OrderEvent oe = OrderEvent.builder()
							.order(order)
							.type("ORDER_REVERSED")
							.build();
					Message<OrderEvent> message = MessageBuilder
							.withPayload(oe)
							.setHeader(KafkaHeaders.TOPIC,"reversed-order")
							.build();
					kafkaOrderTemplate.send(message);
				});
				//.onErrorMap(throwable -> new Exception(String.format("Error occurred when saving payment.Cause %s", throwable)));
	}

	@KafkaListener(topics = "reversed-payment", groupId = "payment-group")
	@Override
	public void reversePayment(OrderEvent paymentEvent) throws JsonProcessingException {
		log.info("Inside reverse payment for order {}", paymentEvent);
		OrderDto order = paymentEvent.getOrder();
		repository.findByOrderId(order.getOrderId())
				.flatMap(payment -> {
					payment.setStatus("FAILED");
					return repository.save(payment);
				})
				.subscribe(
						payment -> {
							OrderEvent orderEvent = OrderEvent
									.builder()
									.order(paymentEvent.getOrder())
									.type("ORDER_REVERSED")
									.build();
							Message<OrderEvent> message = MessageBuilder
									.withPayload(orderEvent)
									.setHeader(KafkaHeaders.TOPIC,"reversed-order")
									.build();
							kafkaOrderTemplate.send(message);
						},
						error -> {
							// Handle error
							System.err.println("Error occurred: " + error.getMessage());
						},
						() -> {
							// Completion handling
							System.out.println("All orders reversed successfully.");
						}
				);
	}
}
