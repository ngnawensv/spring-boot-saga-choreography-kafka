package com.delivery.svc.service.impl;

import com.delivery.svc.dto.CustomerOrder;
import com.delivery.svc.dto.DeliveryEvent;
import com.delivery.svc.entity.Delivery;
import com.delivery.svc.repository.DeliveryRepository;
import com.delivery.svc.service.DeliverOrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeliverOrderServiceImpl implements DeliverOrderService {

    private final DeliveryRepository repository;
    private final KafkaTemplate<String, DeliveryEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public DeliverOrderServiceImpl(DeliveryRepository repository, KafkaTemplate<String, DeliveryEvent> kafkaTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    @Override
    public void deliverOrder(String event) throws Exception {
        log.info("Inside ship order for order {}",event);

        Delivery shipment = new Delivery();
        DeliveryEvent inventoryEvent = objectMapper.readValue(event, DeliveryEvent.class);
        CustomerOrder order = inventoryEvent.getOrder();
        if (Strings.isBlank(order.getAddress())) {
            throw new Exception("Address not present");
        }
        shipment.setAddress(order.getAddress());
        shipment.setOrderId(order.getOrderId());
        shipment.setStatus("success");
        repository.save(shipment)
                .doOnSuccess(shipmentSaved-> {
                    log.info("shipment successfully save {}",shipmentSaved);
                })
                .doOnError(throwable -> {
                    shipment.setOrderId(order.getOrderId());
                    shipment.setStatus("FAILED");
                    repository.save(shipment);

                    log.info("order {} ",order);

                    DeliveryEvent reverseEvent = new DeliveryEvent();
                    reverseEvent.setType("STOCK_REVERSED");
                    reverseEvent.setOrder(order);
                    kafkaTemplate.send("reversed-stock", reverseEvent);
                })
                .onErrorMap(throwable -> new Exception(String.format("Error occurred when creating order.Cause %s", throwable)));
    }

}

