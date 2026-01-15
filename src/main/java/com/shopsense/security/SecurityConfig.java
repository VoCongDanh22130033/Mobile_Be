package com.shopsense.security;

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

import com.shopsense.model.Role;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	JwtAuthFilter jwtAuthFilter;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						//PUBLIC endpoints (KHÔNG cần token)
						.requestMatchers(
								"/admin/login",
								"/customer/login",
								"/customer/signup",
								"/customer/login/firebase",
								"/ping",
								"/error",
								"/uploads/**",
								"/reports/**"
						).permitAll()

						//OPTIONS preflight (nếu cần)
						.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

						//Role-based endpoints
						.requestMatchers("/admin/**").hasAuthority(Role.ADMIN.name())
						.requestMatchers("/seller/**").hasAuthority(Role.SELLER.name())
						.requestMatchers("/customer/**").hasAuthority(Role.CUSTOMER.name())

						//còn lại public
						.requestMatchers(
								"/coupon/check",
								"/collectionpoint/search",
								"/upload",
								"/**"
						).permitAll()

						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	// Mã hóa mật khẩu
}
