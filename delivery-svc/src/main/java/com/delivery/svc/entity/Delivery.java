package com.delivery.svc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Delivery {
    @Id
    private String id;
    private String address;
    private String status;
    private String orderId;

}
