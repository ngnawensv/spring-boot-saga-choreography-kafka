package com.payment.svc.dto;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDto implements Serializable {

	private String item;
	private int quantity;
	private double amount;
	private String paymentMode;
	private String orderId;
	private String address;

}
