package com.stock.svc.repository;

import com.stock.svc.entity.WareHouse;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.CrudRepository;
import reactor.core.publisher.Flux;

public interface StockRepository extends ReactiveMongoRepository<WareHouse, String> {

	Flux<WareHouse> findByItem(String item);
}
