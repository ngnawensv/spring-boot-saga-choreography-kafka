package com.order.svc.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDto {
	private String item;
	private int quantity;
	private double amount;
	private String paymentMode;
	private String orderId;
	private String address;

}
