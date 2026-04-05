package com.trip.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String tripName;
	private Double totalExpense;
	private Long createdBy;

	private LocalDateTime createdAt = LocalDateTime.now();
	@OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
	@JsonIgnore // ✅ ADD THIS
	private List<Expense> expenses;
	@OneToMany
	private List<User> members;

	public List<User> getMembers() {
		return members;
	}

	public Trip(Long id, String name, String tripName, Double totalExpense, Long createdBy, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.name = name;
		this.tripName = tripName;
		this.totalExpense = totalExpense;
		this.createdBy = createdBy;
		this.createdAt = createdAt;
	}

	public String getTripName() {
		return tripName;
	}

	public void setTripName(String tripName) {
		this.tripName = tripName;
	}

	public Double getTotalExpense() {
		return totalExpense;
	}

	public void setTotalExpense(Double totalExpense) {
		this.totalExpense = totalExpense;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "Trip [id=" + id + ", name=" + name + ", tripName=" + tripName + ", totalExpense=" + totalExpense
				+ ", createdBy=" + createdBy + ", createdAt=" + createdAt + "]";
	}

}
