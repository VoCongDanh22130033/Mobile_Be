package com.shopsense.service;

import com.shopsense.model.Admin;
import com.shopsense.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.shopsense.dao.AdminDA;
import com.shopsense.dao.CustomerDA;
import com.shopsense.dao.SellerDA;
import com.shopsense.dto.AuthRequest;
import com.shopsense.dto.AuthResponse;
import com.shopsense.security.JwtService;

@Service
public class AuthService {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	JwtService jwtService;

	@Autowired
	AdminDA adminDA;

	@Autowired
	CustomerDA customerDA;

	@Autowired
	SellerDA sellerDA;

	public AuthResponse login(AuthRequest a) {
		// Step 1: Kiểm tra tài khoản + mật khẩu
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(a.getEmail(), a.getPassword())
		);

		// Step 2: tìm user từ tất cả bảng
		Object user = null;
		String role = "";

		var admin = adminDA.findByEmail(a.getEmail());
		if (admin != null) {
			user = admin;
			role = "ADMIN";
		}

		var customer = customerDA.findByEmail(a.getEmail());
		if (customer != null) {
			user = customer;
			role = "CUSTOMER";
		}

		// Nếu không tìm thấy user
		if (user == null) {
//			throw new RuntimeException("User not found");
		}

		// Xoá password
		if (user instanceof Admin) ((Admin) user).setPassword(null);
		if (user instanceof Customer) ((Customer) user).setPassword(null);

		// Step 3: Generate token
		var token = jwtService.generateToken((UserDetails) user);

		// Step 4: Trả response toàn bộ
		return AuthResponse.builder()
				.status("success")
				.token(token)
				.role(role)
				.user(user)  // Trả về Object, không cast
				.build();
	}
}
