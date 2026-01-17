package com.shopsense.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopsense.dao.AdminDA;
import com.shopsense.dto.AuthRequest;
import com.shopsense.dto.AuthResponse;
import com.shopsense.dto.StatusUpdate;
import com.shopsense.model.Customer;
import com.shopsense.model.Order;
import com.shopsense.model.OrderDetails;
import com.shopsense.model.Product;
import com.shopsense.model.Role;
import com.shopsense.model.Seller;
import com.shopsense.service.AuthService;

@CrossOrigin(origins = "*") // Allow all origins for mobile app
@RestController
public class AdminController {

	@Autowired
	AdminDA da;

	@Autowired
	AuthService authService;

	@PostMapping(value = "/admin/login")
	public AuthResponse login(@RequestBody AuthRequest a) {
		return authService.login(a);
	}

	@GetMapping(value = "/admin/products")
	public List<Product> getAllProducts() {
		return da.getAllProducts();
	}

	@PostMapping(value = "/admin/product")
	public Product createProduct(@RequestBody Product a) {
		return da.createProduct(a);
	}

	@PutMapping(value = "/admin/product")
	public Product updateProduct(@RequestBody Product a) {
		return da.updateProduct(a);
	}

	@PutMapping(value = "/admin/product/{id}")
	public Product updateProductById(@PathVariable("id") int id, @RequestBody Product a) {
		a.setId(id); // Ensure ID from path is set
		return da.updateProduct(a);
	}

	@DeleteMapping(value = "/admin/product/{id}")
	public boolean deleteProduct(@PathVariable("id") int id) {
		return da.deleteProduct(id);
	}

	@GetMapping(value = "/admin/sellers")
	public List<Seller> getAllSellers() {
		return da.getAllSellers();
	}

	@PutMapping(value = "/admin/seller")
	public StatusUpdate updateSeller(@RequestBody StatusUpdate a) {
		return da.updateSeller(a);
	}

	@GetMapping(value = "/admin/customers")
	public List<Customer> getAllCustomers() {
		return da.getAllCustomers();
	}

	@PostMapping(value = "/admin/customer")
	public Customer createCustomer(@RequestBody Customer a) {
		// Handle role mapping: Frontend sends "USER" but backend uses "CUSTOMER"
		// If role is null or invalid, default to CUSTOMER
		if (a.getRole() == null) {
			a.setRole(Role.CUSTOMER);
		}
		// Note: If frontend sends role as String "USER", Jackson will fail to deserialize
		// This assumes frontend sends valid enum values or we handle it via custom deserializer
		return da.createCustomer(a);
	}

	@PutMapping(value = "/admin/customer")
	public StatusUpdate updateCustomerStatus(@RequestBody StatusUpdate a) {
		return da.updateCustomer(a);
	}

	@PutMapping(value = "/admin/customer/{id}")
	public Customer updateCustomer(@PathVariable("id") int id, @RequestBody Customer a) {
		a.setId(id); // Ensure ID from path is set
		// Handle role mapping: Frontend sends "USER" but backend uses "CUSTOMER"
		if (a.getRole() == null) {
			a.setRole(Role.CUSTOMER);
		}
		return da.updateCustomerFull(a);
	}

	@DeleteMapping(value = "/admin/customer/{id}")
	public boolean deleteCustomer(@PathVariable("id") int id) {
		return da.deleteCustomer(id);
	}

	@GetMapping(value = "/admin/orders")
	public List<Order> getOrders() {
		return da.getOrders();
	}

	@GetMapping(value = "/admin/order")
	public Order getOrder(@RequestParam("orderid") int orderId) {
		return da.getOrder(orderId);
	}

	@PutMapping(value = "/admin/order")
	public boolean updateOrder(@RequestBody OrderDetails p) {
		return da.updateOrder(p);
	}

	@GetMapping(value = "/admin/orders/shipped")
	public List<Order> getShippedOrders() {
		return da.getShippedOrders();
	}
}