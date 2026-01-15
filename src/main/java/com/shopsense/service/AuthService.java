package com.shopsense.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.shopsense.dao.AdminDA;
import com.shopsense.dao.CustomerDA;
import com.shopsense.dao.SellerDA;
import com.shopsense.dto.AuthRequest;
import com.shopsense.dto.AuthResponse;
import com.shopsense.model.Admin;
import com.shopsense.model.Customer;
import com.shopsense.model.Role;
import com.shopsense.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

	@Autowired
	PasswordEncoder passwordEncoder;

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

		// Xoá password trước khi trả về
		if (user instanceof Admin) ((Admin) user).setPassword(null);
		if (user instanceof Customer) ((Customer) user).setPassword(null);

		// Step 3: Generate token
		// ⚠️ Lưu ý: dòng này chỉ an toàn khi user implements UserDetails
		// (Trong code bạn, Customer implements UserDetails. Admin nếu không implements -> sẽ lỗi.)
		var token = jwtService.generateToken((UserDetails) user);

		return AuthResponse.builder()
				.status("success")
				.token(token)
				.role(role)
				.user(user)
				.build();
	}

	/**
	 * ✅ Social login (Google/Facebook) qua Firebase ID Token
	 * - Verify idToken bằng Firebase Admin
	 * - Tìm Customer theo email
	 * - Nếu chưa có thì tạo Customer mới (password random)
	 * - Generate JWT hệ thống từ Customer (UserDetails)
	 */
	public AuthResponse loginWithFirebase(String idToken) {
		try {
			if (idToken == null || idToken.isBlank()) {
				return AuthResponse.builder()
						.status("error")
						.token(null)
						.role("")
						.user(null)
						.build();
			}

			// 1) Verify Firebase ID token
			FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);

			String email = decoded.getEmail();
			String name = decoded.getName();

			if (email == null || email.isBlank()) {
				return AuthResponse.builder()
						.status("error")
						.token(null)
						.role("")
						.user(null)
						.build();
			}

			if (name == null || name.isBlank()) {
				name = email.split("@")[0];
			}

			// 2) Chỉ xử lý CUSTOMER cho mobile (tránh cast lỗi Admin/Seller)
			Customer customer = customerDA.findByEmail(email);

			// 3) Nếu chưa có => tạo Customer mới
			if (customer == null) {
				Customer c = new Customer();
				c.setName(name);
				c.setEmail(email);
				c.setRole(Role.CUSTOMER);
				c.setAddress("");
				c.setImg("default-avatar.png");

				// password bắt buộc để insert
				c.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

				customer = customerDA.signup(c);
			}

			// 4) Không trả password
			customer.setPassword(null);

			// 5) Generate JWT
			String token = jwtService.generateToken(customer);

			return AuthResponse.builder()
					.status("success")
					.token(token)
					.role("CUSTOMER")
					.user(customer)
					.build();

		} catch (Exception e) {
			// ✅ In lỗi ra để bạn biết verify fail ở đâu
			e.printStackTrace();

			return AuthResponse.builder()
					.status("error")
					.token(null)
					.role("")
					.user(null)
					.build();
		}
	}
}
