package com.payment.svc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document(collection = "payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment implements Serializable{

	@Id
	private String id;
	private String mode;
	private String orderId;
	private double amount;
	private String status;

}
