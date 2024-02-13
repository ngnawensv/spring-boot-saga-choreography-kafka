package com.delivery.svc.repository;

import com.delivery.svc.entity.Delivery;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DeliveryRepository extends ReactiveMongoRepository<Delivery, String> {

}
