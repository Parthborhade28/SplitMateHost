package com.trip.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class TripMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tripId;
    private Long userId;

	public TripMember() {
		super();
		// TODO Auto-generated constructor stub
	}
	public TripMember(Long id, Long tripId, Long userId) {
		super();
		this.id = id;
		this.tripId = tripId;
		this.userId = userId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getTripId() {
		return tripId;
	}
	public void setTripId(Long tripId) {
		this.tripId = tripId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	@Override
	public String toString() {
		return "TripMember [id=" + id + ", tripId=" + tripId + ", userId=" + userId + "]";
	}
    
}

