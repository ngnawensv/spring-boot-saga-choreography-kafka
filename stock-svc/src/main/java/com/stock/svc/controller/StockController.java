package com.stock.svc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.stock.svc.dto.Stock;
import com.stock.svc.service.StockService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StockController {

    private StockService stockService;

    @KafkaListener(topics = "new-payment", groupId = "payment-group")
    public void updateStock(String paymentEvent) throws JsonMappingException, JsonProcessingException {
        stockService.updateStock(paymentEvent);
    }

    @PostMapping("/addItems")
    public void addItems(@RequestBody Stock stock) {
        stockService.addItems(stock);
    }
}
