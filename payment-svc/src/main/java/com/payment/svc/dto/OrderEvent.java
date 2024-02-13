package com.payment.svc.dto;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderEvent implements Serializable {

	private String type;
	private OrderDto order;

}
