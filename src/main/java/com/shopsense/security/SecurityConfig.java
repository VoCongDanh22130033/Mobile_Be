package com.shopsense.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.shopsense.model.Role;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	JwtAuthFilter jwtAuthFilter;

	// 1. CẤU HÌNH CORS (Sửa lỗi Access-Control-Allow-Origin)
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// Cho phép tất cả các nguồn (quan trọng khi dùng Ngrok/Flutter)
		configuration.setAllowedOrigins(List.of("*"));
		// Cho phép tất cả các method: GET, POST, PUT, DELETE, OPTIONS
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		// Cho phép tất cả các header
		configuration.setAllowedHeaders(List.of("*"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable()) // Tắt CSRF vì dùng JWT
				// Kích hoạt CORS với cấu hình bên trên
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						// --- A. CÁC API PUBLIC (Không cần Token) ---
						.requestMatchers(
								"/admin/login",
								"/customer/login",
								"/customer/signup",
								"/customer/login/firebase",
								"/ping",
								"/error",
								"/uploads/**",
								"/reports/**",
								"/coupon/check",
								"/collectionpoint/search",
								"/api/payment/**" // Thêm cái này để VNPAY callback không bị chặn
						).permitAll()

						// --- B. PREFLIGHT REQUEST (Cho trình duyệt check CORS) ---
						.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

						// --- C. API PHÂN QUYỀN ---
						.requestMatchers("/admin/**").hasAuthority(Role.ADMIN.name())
						.requestMatchers("/seller/**").hasAuthority(Role.SELLER.name())
						// Lưu ý: Các API customer cụ thể (login/signup) đã được permitAll ở trên
						// Dòng này sẽ bảo vệ các API còn lại của customer (ví dụ: get profile, update)
						.requestMatchers("/customer/**").hasAuthority(Role.CUSTOMER.name())

						// --- D. CÁC API KHÁC ---
						// Bất kỳ request nào khác đều phải đăng nhập
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	// 2. BEAN MÃ HÓA MẬT KHẨU
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// 3. BEAN AUTHENTICATION MANAGER (Để inject vào AuthService)
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}