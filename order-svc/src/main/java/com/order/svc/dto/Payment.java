package com.order.svc.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {

	private String mode;
	private String orderId;
	private double amount;

}
