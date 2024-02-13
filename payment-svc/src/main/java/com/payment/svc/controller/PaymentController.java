package com.payment.svc.controller;

import com.payment.svc.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class PaymentController {

	private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    /*public Mono<Payment> processPayment(OrderEvent orderEvent) throws JsonProcessingException {
        log.info("PaymentController:processPayment:event {} ", orderEvent);
        return paymentService.processPayment(orderEvent);
	}*/
}
