package com.shopsense.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shopsense.db;
import com.shopsense.dto.StatusUpdate;
import com.shopsense.model.Admin;
import com.shopsense.model.Customer;
import com.shopsense.model.Order;
import com.shopsense.model.OrderDetails;
import com.shopsense.model.Product;
import com.shopsense.model.RevenueProfit;
import com.shopsense.model.Role;
import com.shopsense.model.Seller;
import com.shopsense.service.EmailService;

@Service
public class AdminDA {
	PreparedStatement pst;

	@Autowired
	EmailService mailer;

	@Autowired
	PasswordEncoder passwordEncoder;

	public Admin findByEmail(String email) throws UsernameNotFoundException {
		Admin admin = null;
		try {
			pst = db.get().prepareStatement("SELECT id, name, email, role, password FROM admins WHERE email = ?");
			pst.setString(1, email);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				admin = new Admin();
				admin.setId(rs.getInt(1));
				admin.setName(rs.getString(2));
				admin.setEmail(rs.getString(3));
				admin.setRole(Role.valueOf(rs.getString(4)));
				admin.setPassword(rs.getString(5));
			} else {
				throw new UsernameNotFoundException("User not found");
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return admin;
	}

	public List<Product> getAllProducts() {
		List<Product> list = new ArrayList<>();
		try {
			pst = db.get().prepareStatement(
					"SELECT p.id, p.title, p.thumbnail_url, p.description, p.regular_price, p.sale_price, p.category, p.stock_status, p.stock_count, p.status, COALESCE(s.store_name, 'Admin Store') as store_name, p.seller_id " +
							"FROM products p " +
							"LEFT JOIN sellers s ON p.seller_id = s.id");
			ResultSet rs = pst.executeQuery();
			Product p;
			while (rs.next()) {
				p = new Product();
				p.setId(rs.getInt(1));
				p.setTitle(rs.getString(2));
				p.setThumbnailUrl(rs.getString(3));
				p.setDescription(rs.getString(4));
				p.setRegularPrice(rs.getString(5));
				p.setSalePrice(rs.getString(6));
				p.setCategory(rs.getString(7));
				p.setStockStatus(rs.getString(8));
				p.setStockCount(rs.getString(9));
				p.setStatus(rs.getString(10));
				p.setStoreName(rs.getString(11));
				p.setSellerId(rs.getInt(12));
				list.add(p);
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace(); // In lỗi để debug
		}
		return list;
	}

	public Product createProduct(Product a) {
		try {
			// Validation
			if (a.getTitle() == null || a.getTitle().trim().isEmpty()) {
				System.out.println("Error: Product title is required");
				return null;
			}
			if (a.getRegularPrice() == null || a.getRegularPrice().trim().isEmpty()) {
				System.out.println("Error: Product regular price is required");
				return null;
			}
			if (a.getThumbnailUrl() == null || a.getThumbnailUrl().trim().isEmpty()) {
				System.out.println("Error: Product thumbnail is required");
				return null;
			}
			
			pst = db.get().prepareStatement(
					"INSERT INTO products (title, thumbnail_url, description, regular_price, sale_price, category, stock_status, stock_count, seller_id, status)"
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			pst.setString(1, a.getTitle().trim());
			pst.setString(2, a.getThumbnailUrl() != null ? a.getThumbnailUrl().trim() : "");
			pst.setString(3, a.getDescription() != null ? a.getDescription().trim() : "");
			pst.setString(4, a.getRegularPrice().trim());
			pst.setString(5, a.getSalePrice() != null && !a.getSalePrice().trim().isEmpty() ? a.getSalePrice().trim() : a.getRegularPrice().trim());
			pst.setString(6, a.getCategory() != null ? a.getCategory().trim() : "Other");
			pst.setString(7, a.getStockStatus() != null ? a.getStockStatus().trim() : "IN_STOCK");
			pst.setString(8, a.getStockCount() != null && !a.getStockCount().trim().isEmpty() ? a.getStockCount().trim() : "0");
			pst.setInt(9, a.getSellerId() > 0 ? a.getSellerId() : 1);
			pst.setString(10, a.getStatus() != null && !a.getStatus().trim().isEmpty() ? a.getStatus().trim() : "ACTIVE");
			int x = pst.executeUpdate();
			if (x > 0) {
				// Lấy ID vừa tạo
				pst = db.get().prepareStatement("SELECT LAST_INSERT_ID()");
				ResultSet rs = pst.executeQuery();
				if (rs.next()) {
					a.setId(rs.getInt(1));
				}
				return a;
			}
		} catch (Exception e) {
			System.out.println("Error creating product: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public Product updateProduct(Product a) {
		try {
			// Validation
			if (a.getId() <= 0) {
				System.out.println("Error: Product ID is required for update");
				return null;
			}
			if (a.getTitle() == null || a.getTitle().trim().isEmpty()) {
				System.out.println("Error: Product title is required");
				return null;
			}
			if (a.getRegularPrice() == null || a.getRegularPrice().trim().isEmpty()) {
				System.out.println("Error: Product regular price is required");
				return null;
			}
			
			pst = db.get().prepareStatement(
					"UPDATE products SET title = ?, thumbnail_url = ?, description = ?, regular_price = ?, sale_price = ?, category = ?, stock_status = ?, stock_count = ?, status = ? WHERE id = ?");
			pst.setString(1, a.getTitle().trim());
			pst.setString(2, a.getThumbnailUrl() != null ? a.getThumbnailUrl().trim() : "");
			pst.setString(3, a.getDescription() != null ? a.getDescription().trim() : "");
			pst.setString(4, a.getRegularPrice().trim());
			pst.setString(5, a.getSalePrice() != null && !a.getSalePrice().trim().isEmpty() ? a.getSalePrice().trim() : a.getRegularPrice().trim());
			pst.setString(6, a.getCategory() != null ? a.getCategory().trim() : "Other");
			pst.setString(7, a.getStockStatus() != null ? a.getStockStatus().trim() : "IN_STOCK");
			pst.setString(8, a.getStockCount() != null && !a.getStockCount().trim().isEmpty() ? a.getStockCount().trim() : "0");
			pst.setString(9, a.getStatus() != null && !a.getStatus().trim().isEmpty() ? a.getStatus().trim() : "ACTIVE");
			pst.setInt(10, a.getId());
			int x = pst.executeUpdate();
			if (x > 0) {
				return a;
			}
		} catch (Exception e) {
			System.out.println("Error updating product: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public boolean deleteProduct(int id) {
		boolean success = false;
		try {
			pst = db.get().prepareStatement("DELETE FROM products WHERE id = ?");
			pst.setInt(1, id);
			int r = pst.executeUpdate();
			if (r != -1) {
				success = true;
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return success;
	}

	public List<Seller> getAllSellers() {
		List<Seller> list = new ArrayList<>();
		try {
			pst = db.get().prepareStatement("SELECT seller_id, name, email, store_name, balance, status, role FROM sellers");
			ResultSet rs = pst.executeQuery();
			Seller a;
			while (rs.next()) {
				a = new Seller();
				a.setId(rs.getInt("seller_id"));
				a.setName(rs.getString("name"));
				a.setEmail(rs.getString("email"));
				a.setStoreName(rs.getString("store_name"));
				a.setBalance(rs.getDouble("balance"));
				a.setStatus(rs.getString("status"));
				a.setRole(Role.valueOf(rs.getString("role")));
				list.add(a);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return list;
	}

	public StatusUpdate updateSeller(StatusUpdate a) {
		try {
			pst = db.get().prepareStatement("UPDATE sellers SET status = ? WHERE seller_id = ?");
			pst.setString(1, a.getStatus());
			pst.setInt(2, a.getId());
			int x = pst.executeUpdate();
			if (x != -1) {
				return a;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public List<Customer> getAllCustomers() {
		List<Customer> list = new ArrayList<>();
		try {
			pst = db.get().prepareStatement("SELECT id, name, email, status, role FROM customers");
			ResultSet rs = pst.executeQuery();
			Customer a;
			while (rs.next()) {
				a = new Customer();
				a.setId(rs.getInt("id"));
				a.setName(rs.getString("name"));
				a.setEmail(rs.getString("email"));
				a.setStatus(rs.getString("status"));
				a.setRole(Role.valueOf(rs.getString("role")));
				list.add(a);
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return list;
	}

	public Customer createCustomer(Customer c) {
		try {
			// Validation
			if (c.getName() == null || c.getName().trim().isEmpty()) {
				System.out.println("Error: Customer name is required");
				return null;
			}
			if (c.getEmail() == null || c.getEmail().trim().isEmpty()) {
				System.out.println("Error: Customer email is required");
				return null;
			}
			// Validate email format
			Pattern emailPattern = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
			Matcher emailMatcher = emailPattern.matcher(c.getEmail().trim());
			if (!emailMatcher.matches()) {
				System.out.println("Error: Invalid email format");
				return null;
			}
			if (c.getPassword() == null || c.getPassword().trim().isEmpty()) {
				System.out.println("Error: Customer password is required");
				return null;
			}
			if (c.getPassword().trim().length() < 6) {
				System.out.println("Error: Password must be at least 6 characters");
				return null;
			}
			
			pst = db.get().prepareStatement(
					"INSERT INTO customers (name, email, password, role, address, img, status, email_verified) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			pst.setString(1, c.getName().trim());
			pst.setString(2, c.getEmail().trim().toLowerCase());
			// Mã hóa password
			String encodedPassword = passwordEncoder.encode(c.getPassword().trim());
			pst.setString(3, encodedPassword);
			pst.setString(4, c.getRole() != null ? c.getRole().name() : "CUSTOMER");
			pst.setString(5, c.getAddress() != null ? c.getAddress().trim() : "");
			pst.setString(6, c.getImg() != null ? c.getImg().trim() : "default-avatar.png");
			pst.setString(7, c.getStatus() != null ? c.getStatus().trim() : "ACTIVE");
			pst.setBoolean(8, c.isEmailVerified());
			int x = pst.executeUpdate();
			if (x > 0) {
				// Lấy ID vừa tạo
				pst = db.get().prepareStatement("SELECT LAST_INSERT_ID()");
				ResultSet rs = pst.executeQuery();
				if (rs.next()) {
					c.setId(rs.getInt(1));
				}
				c.setPassword(null); // Không trả về password
				return c;
			}
		} catch (Exception e) {
			System.out.println("Error creating customer: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public Customer updateCustomer(Customer c) {
		try {
			// Validation
			if (c.getId() <= 0) {
				System.out.println("Error: Customer ID is required for update");
				return null;
			}
			if (c.getName() == null || c.getName().trim().isEmpty()) {
				System.out.println("Error: Customer name is required");
				return null;
			}
			if (c.getEmail() == null || c.getEmail().trim().isEmpty()) {
				System.out.println("Error: Customer email is required");
				return null;
			}
			// Validate email format
			Pattern emailPattern = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
			Matcher emailMatcher = emailPattern.matcher(c.getEmail().trim());
			if (!emailMatcher.matches()) {
				System.out.println("Error: Invalid email format");
				return null;
			}
			// Validate password if provided
			if (c.getPassword() != null && !c.getPassword().trim().isEmpty() && c.getPassword().trim().length() < 6) {
				System.out.println("Error: Password must be at least 6 characters");
				return null;
			}
			
			// Kiểm tra xem có cập nhật password không
			if (c.getPassword() != null && !c.getPassword().trim().isEmpty()) {
				pst = db.get().prepareStatement(
						"UPDATE customers SET name = ?, email = ?, password = ?, role = ?, address = ?, status = ? WHERE id = ?");
				pst.setString(1, c.getName().trim());
				pst.setString(2, c.getEmail().trim().toLowerCase());
				pst.setString(3, passwordEncoder.encode(c.getPassword().trim()));
				pst.setString(4, c.getRole() != null ? c.getRole().name() : "CUSTOMER");
				pst.setString(5, c.getAddress() != null ? c.getAddress().trim() : "");
				pst.setString(6, c.getStatus() != null ? c.getStatus().trim() : "ACTIVE");
				pst.setInt(7, c.getId());
			} else {
				// Không cập nhật password
				pst = db.get().prepareStatement(
						"UPDATE customers SET name = ?, email = ?, role = ?, address = ?, status = ? WHERE id = ?");
				pst.setString(1, c.getName().trim());
				pst.setString(2, c.getEmail().trim().toLowerCase());
				pst.setString(3, c.getRole() != null ? c.getRole().name() : "CUSTOMER");
				pst.setString(4, c.getAddress() != null ? c.getAddress().trim() : "");
				pst.setString(5, c.getStatus() != null ? c.getStatus().trim() : "ACTIVE");
				pst.setInt(6, c.getId());
			}
			int x = pst.executeUpdate();
			if (x > 0) {
				c.setPassword(null); // Không trả về password
				return c;
			}
		} catch (Exception e) {
			System.out.println("Error updating customer: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public boolean deleteCustomer(int id) {
		boolean success = false;
		try {
			pst = db.get().prepareStatement("DELETE FROM customers WHERE id = ?");
			pst.setInt(1, id);
			int r = pst.executeUpdate();
			if (r != -1) {
				success = true;
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return success;
	}

	public StatusUpdate updateCustomer(StatusUpdate a) {
		try {
			pst = db.get().prepareStatement("UPDATE customers SET status = ? WHERE id = ?");
			pst.setString(1, a.getStatus());
			pst.setInt(2, a.getId());
			int x = pst.executeUpdate();
			if (x != -1) {
				return a;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public List<Order> getOrders() {
		try {
			pst = db.get().prepareStatement(
					"SELECT order_id, order_date, order_total, customer_id, discount, shipping_charge, tax, shipping_street, shipping_city, shipping_post_code, shipping_state, shipping_country, orders.status, orders.sub_total, payment_status, payment_method, card_number, card_cvv, card_holder_name, card_expiry_date, gateway_fee FROM orders JOIN order_details USING(order_id) ORDER BY order_id DESC");
			ResultSet rs = pst.executeQuery();
			List<Order> o = new ArrayList<>();
			Order a;
			while (rs.next()) {
				a = new Order();
				a.setId(rs.getInt(1));
				a.setOrderDate(rs.getDate(2));
				a.setOrderTotal(rs.getDouble(3));
				a.setCustomerId(rs.getInt(4));
				a.setDiscount(rs.getDouble(5));
				a.setShippingCharge(rs.getDouble(6));
				a.setTax(rs.getDouble(7));
				a.setShippingStreet(rs.getString(8));
				a.setShippingCity(rs.getString(9));
				a.setShippingPostCode(rs.getString(10));
				a.setShippingState(rs.getString(11));
				a.setShippingCountry(rs.getString(12));
				a.setStatus(rs.getString(13));
				a.setSubTotal(rs.getDouble(14));
				a.setPaymentStatus(rs.getString(15));
				a.setPaymentMethod(rs.getString(16));
				a.setCardNumber(rs.getString(17));
				a.setCardCvv(rs.getString(18));
				a.setCardHolderName(rs.getString(19));
				a.setCardExpiryDate(rs.getString(20));
				a.setGatewayFee(rs.getDouble(21));
				o.add(a);
			}
			return o;
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public Order getOrder(int orderId) {
		try {
			pst = db.get().prepareStatement(
					"SELECT order_id, order_date, order_total, customer_id, discount, shipping_charge, tax, shipping_street, shipping_city, shipping_post_code, shipping_state, shipping_country, status, sub_total, payment_status, payment_method, card_number, card_cvv, card_holder_name, card_expiry_date, gateway_fee FROM orders WHERE order_id = ?");
			pst.setInt(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				Order a = new Order();
				a.setId(rs.getInt(1));
				a.setOrderDate(rs.getDate(2));
				a.setOrderTotal(rs.getDouble(3));
				a.setCustomerId(rs.getInt(4));
				a.setDiscount(rs.getDouble(5));
				a.setShippingCharge(rs.getDouble(6));
				a.setTax(rs.getDouble(7));
				a.setShippingStreet(rs.getString(8));
				a.setShippingCity(rs.getString(9));
				a.setShippingPostCode(rs.getString(10));
				a.setShippingState(rs.getString(11));
				a.setShippingCountry(rs.getString(12));
				a.setStatus(rs.getString(13));
				a.setSubTotal(rs.getDouble(14));
				a.setPaymentStatus(rs.getString(15));
				a.setPaymentMethod(rs.getString(16));
				a.setCardNumber(rs.getString(17));
				a.setCardCvv(rs.getString(18));
				a.setCardHolderName(rs.getString(19));
				a.setCardExpiryDate(rs.getString(20));
				a.setGatewayFee(rs.getDouble(21));

				PreparedStatement pst2 = db.get().prepareStatement(
						"SELECT order_details_id, order_id, product_id, seller_id, store_name, product_name, product_unit_price, product_thumbnail_url, status, quantity, sub_total, delivery_date FROM order_details WHERE order_id = ?");
				pst2.setInt(1, orderId);
				ResultSet rs2 = pst2.executeQuery();
				List<OrderDetails> orderDetails = new ArrayList<>();
				OrderDetails o;
				while (rs2.next()) {
					o = new OrderDetails();
					o.setId(rs2.getInt(1));
					o.setOrderId(rs2.getInt(2));
					o.setProductId(rs2.getInt(3));
					o.setSellerId(rs2.getInt(4));
					o.setStoreName(rs2.getString(5));
					o.setProductName(rs2.getString(6));
					o.setProductUnitPrice(rs2.getDouble(7));
					o.setProductThumbnailUrl(rs2.getString(8));
					o.setStatus(rs2.getString(9));
					o.setQuantity(rs2.getInt(10));
					o.setSubTotal(rs2.getDouble(11));
					o.setDeliveryDate(rs2.getDate(12));
					orderDetails.add(o);
				}
				a.setOrderDetails(orderDetails);
				return a;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public boolean updateOrder(OrderDetails o) {
		boolean success = false;
		try {
			pst = db.get().prepareStatement("UPDATE order_details SET status = ? WHERE order_details_id = ?");
			pst.setString(1, o.getStatus());
			pst.setInt(2, o.getId());
			int r = pst.executeUpdate();
			if (r != -1) {
				success = true;
			}

			// checking any order-details table order is processing or not
			pst = db.get().prepareStatement("SELECT status FROM order_details WHERE order_id = ?");
			pst.setInt(1, o.getOrderId());
			ResultSet rs = pst.executeQuery();
			String status = "Processing";
			while (rs.next()) {
				String s = rs.getString(1);
				if (s.equals("Canceled") || s.equals("Delivered") || s.equals("Refunded")) {
					status = "Completed";
				}
			}

			// update main order table status
			pst = db.get().prepareStatement("UPDATE orders SET status = ? WHERE order_id = ?");
			pst.setString(1, status);
			pst.setInt(2, o.getOrderId());
			pst.executeUpdate();

			// if the orderDetails is delivered then update revenueProfit
			if (o.getStatus().equals("Delivered")) {
				RevenueProfit rp = new RevenueProfit();
				rp.setSellerId(o.getSellerId());
				rp.setOrderId(o.getOrderId());
				rp.setDeliveryDate(o.getDeliveryDate());
				rp.setOrderDetailsId(o.getId());
				rp.setRevenue(o.getSubTotal());
				rp.setCosts(0);
				rp.setPlatformProfit(rp.getRevenue() * .02); // 2% platform commission
				rp.setSellerProfit(rp.getRevenue() - (rp.getCosts() + rp.getPlatformProfit()));

				pst = db.get().prepareStatement(
						"INSERT INTO revenue_profit (seller_id, order_id, order_date, order_details_id, revenue, costs, platform_profit, seller_profit) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
				pst.setInt(1, rp.getSellerId());
				pst.setInt(2, rp.getOrderId());
				pst.setDate(3, rp.getDeliveryDate());
				pst.setInt(4, rp.getOrderDetailsId());
				pst.setDouble(5, rp.getRevenue());
				pst.setDouble(6, rp.getCosts());
				pst.setDouble(7, rp.getPlatformProfit());
				pst.setDouble(8, rp.getSellerProfit());
				pst.executeUpdate();

				// then update seller account balance
				pst = db.get().prepareStatement("UPDATE sellers SET balance = balance + ? WHERE seller_id = ?");
				pst.setDouble(1, rp.getSellerProfit());
				pst.setInt(2, rp.getSellerId());
				pst.executeUpdate();
			}

		} catch (Exception e) {
			System.out.println(e);
		}
		return success;
	}

	public List<Order> getShippedOrders() {
		try {
			pst = db.get().prepareStatement(
					"SELECT order_id, order_date, order_total, customer_id, discount, shipping_charge, tax, shipping_street, shipping_city, shipping_post_code, shipping_state, shipping_country, orders.status, orders.sub_total, payment_status, payment_method, card_number, card_cvv, card_holder_name, card_expiry_date, gateway_fee FROM orders JOIN order_details USING(order_id) ORDER BY order_id DESC");
			ResultSet rs = pst.executeQuery();
			List<Order> o = new ArrayList<>();
			Order a;
			while (rs.next()) {
				a = new Order();
				a.setId(rs.getInt(1));
				a.setOrderDate(rs.getDate(2));
				a.setOrderTotal(rs.getDouble(3));
				a.setCustomerId(rs.getInt(4));
				a.setDiscount(rs.getDouble(5));
				a.setShippingCharge(rs.getDouble(6));
				a.setTax(rs.getDouble(7));
				a.setShippingStreet(rs.getString(8));
				a.setShippingCity(rs.getString(9));
				a.setShippingPostCode(rs.getString(10));
				a.setShippingState(rs.getString(11));
				a.setShippingCountry(rs.getString(12));
				a.setStatus(rs.getString(13));
				a.setSubTotal(rs.getDouble(14));
				a.setPaymentStatus(rs.getString(15));
				a.setPaymentMethod(rs.getString(16));
				a.setCardNumber(rs.getString(17));
				a.setCardCvv(rs.getString(18));
				a.setCardHolderName(rs.getString(19));
				a.setCardExpiryDate(rs.getString(20));
				a.setGatewayFee(rs.getDouble(21));

				// checking any of order details is shipped
				boolean isShipped = false;
				PreparedStatement pst2 = db.get()
						.prepareStatement("SELECT status FROM order_details WHERE order_id = ?");
				pst2.setInt(1, a.getId());
				ResultSet rs2 = pst2.executeQuery();
				while (rs2.next()) {
					if (rs2.getString(1).equals("Shipped")) {
						isShipped = true;
					}
				}

				// if isShipped is true that mean one of order details is shipped
				if (isShipped) {
					o.add(a);
				}
			}
			return o;
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}
}
