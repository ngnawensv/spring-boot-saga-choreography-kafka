package com.delivery.svc.controller;

import com.delivery.svc.service.DeliverOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Controller;

import com.delivery.svc.dto.CustomerOrder;
import com.delivery.svc.dto.DeliveryEvent;
import com.delivery.svc.entity.Delivery;
import com.delivery.svc.repository.DeliveryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DeliveryController {

	private final DeliverOrderService  deliverOrderService;

    public DeliveryController(DeliverOrderService deliverOrderService) {
        this.deliverOrderService = deliverOrderService;
    }

    @KafkaListener(topics = "new-stock", groupId = "stock-group")
	public void deliverOrder(String event) throws Exception {
		deliverOrderService.deliverOrder(event);
	}
}
