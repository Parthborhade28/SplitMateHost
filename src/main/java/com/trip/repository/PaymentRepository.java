package com.trip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trip.entity.Payment;

import jakarta.transaction.Transactional;
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	List<Payment> findByTripId(Long tripId);
	List<Payment> findByFromUserIdOrToUserId(Long fromUserId, Long toUserId);
	@Transactional
	void deleteByTripId(Long tripId);
	
	
	
//	void deleteByPaidBy(Long userId);
	void deleteByFromUserIdOrToUserId(Long fromId, Long toId);
}
