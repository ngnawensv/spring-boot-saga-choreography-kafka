package com.payment.svc.repository;


import com.payment.svc.entity.Payment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PaymentRepository extends ReactiveMongoRepository<Payment, String> {

	Flux<Payment> findByOrderId(String orderId);
}
