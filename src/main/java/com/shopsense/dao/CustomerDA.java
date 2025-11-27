package com.shopsense.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.shopsense.db;
import com.shopsense.model.CartItem;
import com.shopsense.model.Customer;
import com.shopsense.model.Order;
import com.shopsense.model.OrderDetails;
import com.shopsense.model.Product;
import com.shopsense.model.Role;
import com.shopsense.service.EmailService;

@Service
public class CustomerDA {
    PreparedStatement pst;

    @Autowired
    EmailService mailer;
    // find By Email
    public Customer findByEmail(String email) throws UsernameNotFoundException {
        Customer customer = null;
        try {
            pst = db.get().prepareStatement(
                    "SELECT c.id, c.name, c.email, c.role, c.address, c.password, c.img " +
                            "FROM customers c WHERE c.email = ?"
            );

            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                customer = new Customer();
                customer.setId(rs.getInt(1));
                customer.setName(rs.getString(2));
                customer.setEmail(rs.getString(3));
                customer.setRole(Role.valueOf(rs.getString(4)));
                customer.setAddress(rs.getString(5));
                customer.setPassword(rs.getString(6));
                customer.setImg(rs.getString(7)); // ✅ set img
                System.out.println("FindByEmail called with: " + email + ", img=" + customer.getImg());
            } else {
                throw new UsernameNotFoundException("User not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customer;
    }



    // sign up
    public Customer signup(Customer a) {
        try {
            pst = db.get().prepareStatement(
                    "INSERT INTO customers (name, email, password, role, address, img) VALUES (?, ?, ?, ?, ?, ?)"
            );
            pst.setString(1, a.getName());
            pst.setString(2, a.getEmail());
            pst.setString(3, a.getPassword());
            pst.setString(4, a.getRole().name());
            pst.setString(5, a.getAddress());
            pst.setString(6, a.getImg()); // ✅ Lưu ảnh
            int x = pst.executeUpdate();
            if (x != -1) {
                return a;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    //
    public Customer getCustomer(int customerId) {
        Customer p = null;
        try {
            pst = db.get().prepareStatement(
                    "SELECT id, name, email, password, role, address, status, email_verified, img FROM customers WHERE id = ?"
            );
            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                p = new Customer();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setEmail(rs.getString("email"));
                p.setPassword(null);
                p.setRole(Role.valueOf(rs.getString("role")));
                p.setAddress(rs.getString("address"));
                p.setStatus(rs.getString("status"));
                p.setEmailVerified(rs.getBoolean("email_verified"));
                p.setImg(rs.getString("img")); // ✅ Lấy ảnh
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return p;
    }


    public Product getProduct(int productId) {
        Product p = null;
        try {
            pst = db.get().prepareStatement(
                    "SELECT p.id, p.title, p.thumbnail_url, p.description, p.regular_price, p.sale_price, " +
                            "p.category, p.stock_status, p.stock_count, s.id AS seller_id, s.store_name, p.status " +
                            "FROM products p " +
                            "JOIN sellers s ON p.seller_id = s.id " +
                            "WHERE p.id = ? AND p.status = 'Active' AND s.status = 'Active'"
            );

            pst.setInt(1, productId);
            ResultSet rs = pst.executeQuery();
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
                p.setSellerId(rs.getInt(10));
                p.setStoreName(rs.getString(11));
                p.setStatus(rs.getString(12));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return p;
    }

    public List<Product> getProducts() {
        List<Product> list = new ArrayList<>();
        try {
            pst = db.get().prepareStatement(
                    "SELECT p.id, p.title, p.thumbnail_url, p.description, p.regular_price, p.sale_price, " +
                            "p.category_id, p.stock_status, p.stock_count, p.status, p.seller_id, s.store_name " +
                            "FROM products p " +
                            "JOIN sellers s ON p.seller_id = s.id " +
                            "WHERE p.status = 'Active' AND s.status = 'Active'"
            );

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
                p.setSellerId(rs.getInt(11));
                p.setStoreName(rs.getString(12));

                list.add(p);
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }

    // Add Card
    public CartItem addToCart(CartItem a) {
        try {
            pst = db.get().prepareStatement(
                    "INSERT INTO carts (customer_id, product_id, seller_id, store_name, product_name, product_thumbnail_url, product_unit_price, quantity, sub_total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );

            pst.setInt(1, a.getCustomerId());
            pst.setInt(2, a.getProductId());
            pst.setInt(3, a.getSellerId());
            pst.setString(4, a.getStoreName());
            pst.setString(5, a.getProductName());
            pst.setString(6, a.getProductThumbnailUrl());
            pst.setDouble(7, a.getProductUnitPrice());
            pst.setInt(8, a.getProductQuantity());
            pst.setDouble(9, a.getSubTotal());
            int x = pst.executeUpdate();
            if (x != -1) {
                return a;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public boolean updateCart(CartItem a) {
        try {
            pst = db.get().prepareStatement("UPDATE carts SET quantity = ?, sub_total = ? WHERE id = ?");
            pst.setInt(1, a.getProductQuantity());
            pst.setDouble(2, a.getSubTotal());
            pst.setInt(3, a.getId());
            int x = pst.executeUpdate();
            if (x != -1) {
                return true;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public boolean removeFromCart(int id) {
        try {
            pst = db.get().prepareStatement("DELETE FROM carts WHERE id = ?");
            pst.setInt(1, id);
            int x = pst.executeUpdate();
            if (x != -1) {
                return true;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public List<CartItem> getCartItems(int customerId) {
        List<CartItem> list = new ArrayList<>();
        try {
            pst = db.get().prepareStatement(
                    "SELECT c.id, customer_id, product_id, seller_id, store_name, product_name, product_thumbnail_url, product_unit_price, quantity, sub_total FROM carts c WHERE customer_id = ?"
            );

            pst.setInt(1, customerId);
            ResultSet rs = pst.executeQuery();
            CartItem p;
            while (rs.next()) {
                p = new CartItem();
                p.setId(rs.getInt(1));
                p.setCustomerId(rs.getInt(2));
                p.setProductId(rs.getInt(3));
                p.setSellerId(rs.getInt(4));
                p.setStoreName(rs.getString(5));
                p.setProductName(rs.getString(6));
                p.setProductThumbnailUrl(rs.getString(7));
                p.setProductUnitPrice(rs.getDouble(8));
                p.setProductQuantity(rs.getInt(9));
                p.setSubTotal(rs.getDouble(10));
                list.add(p);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }

    public Order placeOrder(Order order) {
        try {
            // 1. Insert vào bảng orders
            pst = db.get().prepareStatement(
                    "INSERT INTO orders (order_date, order_total, customer_id, discount, shipping_charge, tax, shipping_street, shipping_city, shipping_post_code, shipping_state, shipping_country, status, sub_total, payment_status, payment_method, card_number, card_cvv, card_holder_name, card_expiry_date, gateway_fee) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            pst.setDate(1, order.getOrderDate());
            pst.setDouble(2, order.getOrderTotal());
            pst.setInt(3, order.getCustomerId());
            pst.setDouble(4, order.getDiscount());
            pst.setDouble(5, order.getShippingCharge());
            pst.setDouble(6, order.getTax());
            pst.setString(7, order.getShippingStreet());
            pst.setString(8, order.getShippingCity());
            pst.setString(9, order.getShippingPostCode());
            pst.setString(10, order.getShippingState());
            pst.setString(11, order.getShippingCountry());
            pst.setString(12, order.getStatus());
            pst.setDouble(13, order.getSubTotal());
            pst.setString(14, order.getPaymentStatus());
            pst.setString(15, order.getPaymentMethod());
            pst.setString(16, order.getCardNumber());
            pst.setString(17, order.getCardCvv());
            pst.setString(18, order.getCardHolderName());
            pst.setString(19, order.getCardExpiryDate());
            pst.setDouble(20, order.getGatewayFee());

            int rows = pst.executeUpdate();
            if (rows != 0) {
                ResultSet generatedKeys = pst.getGeneratedKeys();
                int orderId = 0;
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                    order.setId(orderId);
                }

                // 2. Insert vào bảng order_details
                List<OrderDetails> detailsList = order.getOrderDetails();
                PreparedStatement pstDetails = db.get().prepareStatement(
                        "INSERT INTO order_details (order_id, product_id, seller_id, store_name, product_name, product_unit_price, product_thumbnail_url, status, quantity, sub_total, delivery_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                );

                for (OrderDetails d : detailsList) {
                    pstDetails.setInt(1, orderId);                     // order_id
                    pstDetails.setInt(2, d.getProductId());            // product_id
                    pstDetails.setInt(3, d.getSellerId());             // seller_id (bổ sung)
                    pstDetails.setString(4, d.getStoreName());         // store_name
                    pstDetails.setString(5, d.getProductName());       // product_name
                    pstDetails.setDouble(6, d.getProductUnitPrice());  // product_unit_price
                    pstDetails.setString(7, d.getProductThumbnailUrl());// product_thumbnail_url
                    pstDetails.setString(8, d.getStatus());            // status
                    pstDetails.setInt(9, d.getQuantity());            // quantity
                    pstDetails.setDouble(10, d.getSubTotal());        // sub_total
                    pstDetails.setDate(11, d.getDeliveryDate());      // delivery_date

                    pstDetails.addBatch();
                }

                pstDetails.executeBatch();

                // 3. Xóa giỏ hàng
                PreparedStatement pstCart = db.get().prepareStatement("DELETE FROM carts WHERE id = ?");
                pstCart.setInt(1, order.getCustomerId());
                pstCart.executeUpdate();

                return order;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // Lấy thông tin chi tiết một đơn hàng cùng danh sách các sản phẩm trong đơn theo order_id (track)

    public Order getOrder(int id) {
        try {
            pst = db.get().prepareStatement(
                    "SELECT id, order_date, order_total, id, discount, shipping_charge, tax, shipping_street, shipping_city, shipping_post_code, shipping_state, shipping_country, status, sub_total, payment_status, payment_method, card_number, card_cvv, card_holder_name, card_expiry_date, gateway_fee FROM orders WHERE id = ?");
            pst.setInt(1, id);
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
                        "SELECT id, order_id, product_id, seller_id, store_name, product_name, product_unit_price, product_thumbnail_url, status, quantity, sub_total, delivery_date FROM order_details WHERE order_id = ?");
                pst2.setInt(1, id);
                ResultSet rs2 = pst2.executeQuery();
                List<OrderDetails> orderDetails = new ArrayList<>();
                OrderDetails o;
                while (rs2.next()) {
                    o = new OrderDetails();
                    o.setId(rs2.getInt("id"));
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

    public List<Order> getOrders(int customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id, order_date, order_total, customer_id, discount, shipping_charge, tax,\n" +
                "       shipping_street, shipping_city, shipping_post_code, shipping_state, shipping_country,\n" +
                "       status, sub_total, payment_status, payment_method, card_number, card_cvv,\n" +
                "       card_holder_name, card_expiry_date, gateway_fee\n" +
                "FROM orders\n" +
                "WHERE customer_id = ?\n" +
                "ORDER BY id DESC;\n";

        try (PreparedStatement pst = db.get().prepareStatement(sql)) {
            pst.setInt(1, customerId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("id"));
                    order.setOrderDate(rs.getDate("order_date"));
                    order.setOrderTotal(rs.getDouble("order_total"));
                    order.setCustomerId(rs.getInt("customer_id"));
                    order.setDiscount(rs.getDouble("discount"));
                    order.setShippingCharge(rs.getDouble("shipping_charge"));
                    order.setTax(rs.getDouble("tax"));
                    order.setShippingStreet(rs.getString("shipping_street"));
                    order.setShippingCity(rs.getString("shipping_city"));
                    order.setShippingPostCode(rs.getString("shipping_post_code"));
                    order.setShippingState(rs.getString("shipping_state"));
                    order.setShippingCountry(rs.getString("shipping_country"));
                    order.setStatus(rs.getString("status"));
                    order.setSubTotal(rs.getDouble("sub_total"));
                    order.setPaymentStatus(rs.getString("payment_status"));
                    order.setPaymentMethod(rs.getString("payment_method"));
                    order.setCardNumber(rs.getString("card_number"));
                    order.setCardCvv(rs.getString("card_cvv"));
                    order.setCardHolderName(rs.getString("card_holder_name"));
                    order.setCardExpiryDate(rs.getString("card_expiry_date"));
                    order.setGatewayFee(rs.getDouble("gateway_fee"));

                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public OrderDetails trackOrder(int orderDetailsId) {
        try {
            pst = db.get().prepareStatement(
                    "SELECT id, order_id, product_id, seller_id, store_name, product_name, product_unit_price, product_thumbnail_url, status, quantity, sub_total, delivery_date FROM order_details WHERE id = ?"
            );
            pst.setInt(1, orderDetailsId);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                OrderDetails od = new OrderDetails();
                od.setId(rs.getInt("id"));
                od.setOrderId(rs.getInt("order_id"));
                od.setProductId(rs.getInt("product_id"));
                od.setSellerId(rs.getInt("seller_id"));
                od.setStoreName(rs.getString("store_name"));
                od.setProductName(rs.getString("product_name"));
                od.setProductUnitPrice(rs.getDouble("product_unit_price"));
                od.setProductThumbnailUrl(rs.getString("product_thumbnail_url"));
                od.setStatus(rs.getString("status"));
                od.setQuantity(rs.getInt("quantity"));
                od.setSubTotal(rs.getDouble("sub_total"));
                od.setDeliveryDate(rs.getDate("delivery_date"));

                return od;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Kiểm tra xem khách hàng có từng mua sản phẩm cụ thể hay không
    public boolean isProductPurchased(int customerId, int productId) {
        try {
            pst = db.get().prepareStatement("""
                    	SELECT COUNT(*)
                    	FROM order_details
                    	JOIN orders USING(order_id)
                    	WHERE id = ? AND id = ?
                    """);
            pst.setInt(1, productId);
            pst.setInt(2, customerId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public List<Product> searchProducts(String q) {
        List<Product> list = new ArrayList<>();
        try {
            pst = db.get().prepareStatement(
                    "SELECT id, title, thumbnail_url, description, regular_price, sale_price, category, stock_status, stock_count, products.status "
                            + "FROM products JOIN sellers USING(id)"
                            + "WHERE products.status = 'Active' AND sellers.status = 'Active' AND title LIKE ?");
            pst.setString(1, "%".concat(q).concat("%"));
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
                list.add(p);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }

    public boolean sendVerificationCode(Customer a) {
        try {
            Random random = new Random();
            int randomCode = random.nextInt(999999 - 100000 + 1) + 100000;
            pst = db.get().prepareStatement("DELETE FROM verification_code WHERE user_id = ?");
            pst.setInt(1, a.getId());
            pst.executeUpdate();
            pst = db.get().prepareStatement("INSERT INTO verification_code (user_id, code) VALUES (?, ?)");
            pst.setInt(1, a.getId());
            pst.setInt(2, randomCode);
            pst.executeUpdate();
            mailer.sendContentEmail("danhv5879#gmail.com", "Verification Code",
                    "<h2>Verification code is : " + String.valueOf(randomCode) + "</h2>");
            return true;
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public boolean verifyCode(int userId, int code) {
        try {
            pst = db.get().prepareStatement("SELECT * FROM verification_code WHERE user_id = ? AND code = ?");
            pst.setInt(1, userId);
            pst.setInt(2, code);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                mailer.sendContentEmail("danhv5879#gmail.com", "Email Verified",
                        "<h2>Email verification is complete</h2>");
                pst = db.get().prepareStatement("DELETE FROM verification_code WHERE user_id = ?");
                pst.setInt(1, userId);
                pst.executeUpdate();
                pst = db.get().prepareStatement("UPDATE customers SET email_verified = true WHERE id = ?");
                pst.setInt(1, userId);
                pst.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }
    public Customer updateCustomer(Customer a) {
        try {
            pst = db.get().prepareStatement(
                    "UPDATE customers SET name=?, email=?, address=?, img=? WHERE id=?"
            );
            pst.setString(1, a.getName());
            pst.setString(2, a.getEmail());
            pst.setString(3, a.getAddress());
            pst.setString(4, a.getImg()); // ✅ Cập nhật ảnh
            pst.setInt(5, a.getId());
            int x = pst.executeUpdate();
            if (x != -1) return a;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

}
