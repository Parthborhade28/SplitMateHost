package com.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
public class AuthResponse {

	private String token;
	private Long id;
	

	

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public AuthResponse(String token, Long id) {
		super();
		this.token = token;
		this.id = id;
	}

	
}
