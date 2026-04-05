package com.trip.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	@Autowired
	JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String header = request.getHeader("Authorization");

		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);

			try {
				String email = jwtUtil.extractEmail(token);

				UsernamePasswordAuthenticationToken auth =
				        new UsernamePasswordAuthenticationToken(
				                email,
				                null,
				                List.of(new SimpleGrantedAuthority("ROLE_USER"))
				        );


				SecurityContextHolder.getContext().setAuthentication(auth);

			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	// ⭐⭐⭐ THIS IS THE MAGIC FIX ⭐⭐⭐
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
	    String path = request.getServletPath();

	    return path.startsWith("/auth")
	        || path.startsWith("/trips")
	        || path.startsWith("/expenses")   // 🔥 ADD THIS
	        || path.startsWith("/users")
	        || path.startsWith("/trip/finish");
	}
}
