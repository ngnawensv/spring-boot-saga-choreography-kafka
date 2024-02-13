package com.payment.svc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.payment.svc.dto.OrderEvent;
import com.payment.svc.entity.Payment;
import reactor.core.publisher.Mono;

public interface PaymentService {
	void processPayment(OrderEvent orderEvent) throws JsonProcessingException;
	void reversePayment(OrderEvent orderEvent) throws JsonProcessingException;
}
