package com.trip.controller;

import com.trip.entity.User;
import com.trip.repository.UserRepository;
import com.trip.service.UserService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	@Autowired
	private UserRepository repo;

	@Autowired
	private UserService userService;

	@GetMapping("/by-email")
	public User getByEmail(@RequestParam String email) {
		return repo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
	}

	// 🔥 SECOND: generic mapping
	@GetMapping("/{id}")
	public User getUser(@PathVariable Long id) {
		return repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
	}

	// UPDATE UPI
	@PutMapping("/{id}/upi")
	public User updateUpi(@PathVariable Long id, @RequestParam String upiId) {

		User user = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

		user.setUpiId(upiId);

		return repo.save(user);
	}

	@DeleteMapping("/delete")
	public String deleteMyAccount() {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		userService.deleteUserByEmail(email);

		return "Account deleted successfully ✅";
	}
	
	@GetMapping("/search")
	public List<User> searchUsers(@RequestParam String keyword) {
	    return repo.findByEmailContainingIgnoreCase(keyword);
	}
}