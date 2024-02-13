package com.stock.svc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "warehouse")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WareHouse {

	@Id
	private String id;
	private int quantity;
	private String item;

}
