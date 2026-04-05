package com.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.trip.entity.Expense;

import jakarta.transaction.Transactional;

import java.util.List;
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
	List<Expense> findByTripId(Long tripId);

	@Transactional
	@Modifying
	void deleteByTripId(Long tripId);
//	void deleteByUserId(Long userId);
	boolean existsByTripIdAndPaidBy(Long tripId, Long userId);
	void deleteByPaidBy(Long userId);
	
	List<Expense> findByPaidBy(Long userId);
	
	List<Expense> findBypaidBy(Long paidBy);
}
