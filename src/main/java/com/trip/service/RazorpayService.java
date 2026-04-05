package com.trip.service;

import com.razorpay.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {


	@Value("${razorpay.key}")
	private String KEY;

	@Value("${razorpay.secret}")
	private String SECRET;

    public String createOrder(Double amount) {

        try {
            RazorpayClient client = new RazorpayClient(KEY, SECRET);

            JSONObject options = new JSONObject();
            options.put("amount", (int)(amount * 100)); // paise
            options.put("currency", "INR");
            options.put("receipt", "txn_123");

            Order order = client.orders.create(options);

            return order.toString();

        } catch (Exception e) {
            throw new RuntimeException("Payment error");
        }
    }
}
