package com.trip.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trip.dto.SettlementDTO;
import com.trip.service.SettlementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/settle")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SettlementController {
	@Autowired
	SettlementService service;

	@GetMapping("/{tripId}")
	public List<SettlementDTO> settle(@PathVariable Long tripId){
	    return service.settle(tripId);
	}

}
