package com.trip.dto;

import com.trip.entity.ExpenseCategory;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ExpenseRequest {
	private Long tripId;
	private Long paidBy;
	private Double amount;
	private String desc;
	private ExpenseCategory category;
}
