package com.stock.svc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.stock.svc.dto.Stock;

public interface StockService {
    void updateStock(String paymentEvent) throws JsonMappingException, JsonProcessingException;
    void addItems(Stock stock);
    void reverseStock(String event) throws JsonProcessingException;
}
