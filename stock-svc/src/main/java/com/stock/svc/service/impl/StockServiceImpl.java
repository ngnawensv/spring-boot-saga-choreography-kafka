package com.stock.svc.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.svc.dto.CustomerOrder;
import com.stock.svc.dto.DeliveryEvent;
import com.stock.svc.dto.PaymentEvent;
import com.stock.svc.dto.Stock;
import com.stock.svc.entity.WareHouse;
import com.stock.svc.repository.StockRepository;
import com.stock.svc.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class StockServiceImpl implements StockService {
    private final StockRepository repository;
    private final KafkaTemplate<String, DeliveryEvent> kafkaTemplate;
    private final KafkaTemplate<String, PaymentEvent> kafkaPaymentTemplate;
    private final ObjectMapper objectMapper;

    public StockServiceImpl(StockRepository repository, KafkaTemplate<String, DeliveryEvent> kafkaTemplate, KafkaTemplate<String, PaymentEvent> kafkaPaymentTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaPaymentTemplate = kafkaPaymentTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void updateStock(String paymentEvent) throws JsonMappingException, JsonProcessingException {
        log.info("Inside update inventory for order {}",paymentEvent);

        DeliveryEvent event = new DeliveryEvent();
        PaymentEvent p = objectMapper.readValue(paymentEvent, PaymentEvent.class);
        CustomerOrder order = p.getOrder();
        repository.findByItem(order.getItem())
                .flatMap(inventory->{
                    inventory.setQuantity(inventory.getQuantity() - order.getQuantity());
                    return repository.save(inventory);
                })
                .subscribe(
                        payment -> {
                            event.setType("STOCK_UPDATED");
                            event.setOrder(p.getOrder());
                            kafkaTemplate.send("new-stock", event);
                        },
                        error -> {
                            // Handle error
                            PaymentEvent pe = new PaymentEvent();
                            pe.setOrder(order);
                            pe.setType("PAYMENT_REVERSED");
                            kafkaPaymentTemplate.send("reversed-payment", pe);
                        },
                        () -> {
                            // Completion handling
                            log.info("Update stock successfully done.");
                        }
                );
    }

    @Override
    public void addItems(@RequestBody Stock stock) {
        Flux<WareHouse> itemsFlux = repository.findByItem(stock.getItem());
        itemsFlux.hasElements()
                .subscribe(hasElements -> {
                    if (hasElements) {
                        itemsFlux.flatMap(item-> {
                            item.setQuantity(stock.getQuantity() + item.getQuantity());
                        return repository.save(item);
                        });
                    } else {
                        WareHouse i = new WareHouse();
                        i.setItem(stock.getItem());
                        i.setQuantity(stock.getQuantity());
                        repository.save(i);
                    }
                });

    }

    @KafkaListener(topics = "reversed-stock", groupId = "stock-group")
    public void reverseStock(String event) throws JsonProcessingException {
        log.info("Inside reverse stock for order {}", event);
        DeliveryEvent deliveryEvent = objectMapper.readValue(event, DeliveryEvent.class);
        var item = deliveryEvent.getOrder().getItem();
        repository.findByItem(item)
                .flatMap(itemInv -> {
                    itemInv.setQuantity(itemInv.getQuantity() + deliveryEvent.getOrder().getQuantity());
                    return repository.save(itemInv);
                }).subscribe(
                        payment -> {
                            PaymentEvent paymentEvent = new PaymentEvent();
                            paymentEvent.setOrder(deliveryEvent.getOrder());
                            paymentEvent.setType("PAYMENT_REVERSED");
                            kafkaPaymentTemplate.send("reversed-payment", paymentEvent);
                        },
                        error -> {
                            // Handle error
                            log.error("Error occurred: {}", error.getMessage());
                        },
                        () -> {
                            // Completion handling
                            log.info("Payment successfully reserved.");
                        }
                );
    }
}
