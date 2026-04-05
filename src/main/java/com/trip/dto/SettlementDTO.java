package com.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementDTO {
	private Long fromUserId; // 🔥 NEW
	private Long toUserId;
	private String fromUser;
	private String toUser;
	private Double amount;

}
