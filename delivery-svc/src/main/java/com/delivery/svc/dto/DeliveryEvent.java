package com.delivery.svc.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryEvent {

	private String type;

	private CustomerOrder order;

}
