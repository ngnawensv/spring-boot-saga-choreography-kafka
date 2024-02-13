package com.payment.svc.dto;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Stock implements Serializable {

	private String item;
	private int quantity;

}
