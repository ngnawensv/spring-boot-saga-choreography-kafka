package com.order.svc.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderEvent {

	private String type;
	private OrderDto order;

}
