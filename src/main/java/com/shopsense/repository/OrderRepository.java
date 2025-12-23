package com.shopsense.repository;

import com.shopsense.entity.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp; // Bắt buộc import cái này

@Repository
public class OrderRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void updatePaymentStatus(int orderId, String status) {
        String sql = "UPDATE orders SET payment_status = ? WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, status, orderId);
            System.out.println("UPDATE ORDERS: Đã cập nhật " + rows + " dòng cho Order ID: " + orderId);
        } catch (Exception e) {
            System.err.println("LỖI UPDATE ORDERS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void savePayment(Payment payment) {
        String sql = "INSERT INTO payments " +
                "(order_id, amount, provider, transaction_ref, transaction_no, bank_code, card_type, pay_date, order_info, response_code, transaction_status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            // Chuyển đổi timestamp
            Timestamp sqlCreateDate = new Timestamp(System.currentTimeMillis());

            // In ra trước khi chạy lệnh
            System.out.println("-> Đang thực thi lệnh SQL INSERT cho OrderID: " + payment.getOrderId());

            int rows = jdbcTemplate.update(sql,
                    payment.getOrderId(),
                    payment.getAmount(),
                    payment.getProvider(),
                    payment.getTransactionRef(),
                    payment.getTransactionNo(),
                    payment.getBankCode(),
                    payment.getCardType(),
                    payment.getPayDate(),
                    payment.getOrderInfo(),
                    payment.getResponseCode(),
                    payment.getTransactionStatus(),
                    sqlCreateDate
            );

            // QUAN TRỌNG: In ra số dòng bị tác động
            System.out.println("-> KẾT QUẢ INSERT: " + rows + " dòng đã được thêm.");

        } catch (Exception e) {
            System.err.println("!!! LỖI NGHIÊM TRỌNG KHI INSERT: ");
            e.printStackTrace(); // Phải in stack trace để biết lỗi gì
        }
    }
}