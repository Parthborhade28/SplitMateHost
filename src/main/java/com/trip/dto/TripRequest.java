package com.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TripRequest {

	@NotBlank(message = "Trip name is required") // 🔥 IMPORTANT
	private String name;

	@NotNull(message = "User ID is required")
	private Long userId;
}