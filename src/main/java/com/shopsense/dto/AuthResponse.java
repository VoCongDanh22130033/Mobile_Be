package com.shopsense.dto;

import com.shopsense.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
	private String status;
	private String token;
	private Customer user;  // ✅ sửa từ Object -> Customer
	private String role;
}
