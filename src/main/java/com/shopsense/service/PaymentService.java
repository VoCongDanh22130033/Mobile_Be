package com.shopsense.service;

import com.shopsense.entity.Payment;
import com.shopsense.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional; // Tạm thời comment dòng này lại

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;

    // @Transactional // Tạm thời bỏ để debug
    // Trong PaymentService.java

    public void processVnPayReturn(Map<String, String> params) {
        System.out.println("DEBUG PARAMS: " + params.toString()); // Xem log để biết VNPAY trả về gì

        String vnp_TxnRef = params.get("vnp_TxnRef"); // Ví dụ: "1" hoặc "1_234234"
        String vnp_Amount = params.get("vnp_Amount");

        // 1. XỬ LÝ ORDER ID (Quan trọng: Tách chuỗi nếu có dấu gạch dưới)
        int orderId = 0;
        try {
            if (vnp_TxnRef != null && vnp_TxnRef.contains("_")) {
                // Nếu mã là "1_2025..." thì chỉ lấy số "1"
                String[] parts = vnp_TxnRef.split("_");
                orderId = Integer.parseInt(parts[0]);
            } else if (vnp_TxnRef != null) {
                orderId = Integer.parseInt(vnp_TxnRef);
            }
        } catch (NumberFormatException e) {
            System.err.println("Lỗi tách Order ID từ TxnRef: " + vnp_TxnRef);
            return; // Dừng lại nếu không lấy được ID
        }

        Payment payment = new Payment();
        payment.setOrderId(orderId); // Set ID chuẩn (số 1)

        // Xử lý số tiền
        try {
            if (vnp_Amount != null) payment.setAmount(Double.parseDouble(vnp_Amount) / 100);
        } catch (Exception e) {}

        payment.setProvider("VNPAY");
        payment.setTransactionRef(vnp_TxnRef); // Lưu nguyên văn mã tham chiếu để tra soát
        payment.setTransactionNo(params.get("vnp_TransactionNo"));
        payment.setBankCode(params.get("vnp_BankCode"));
        payment.setCardType(params.get("vnp_CardType"));
        payment.setPayDate(params.get("vnp_PayDate"));
        payment.setOrderInfo(params.get("vnp_OrderInfo"));
        payment.setResponseCode(params.get("vnp_ResponseCode"));
        payment.setTransactionStatus(params.get("vnp_TransactionStatus"));
        payment.setCreatedAt(LocalDateTime.now());

        // 2. GỌI LỆNH SAVE
        try {
            orderRepository.savePayment(payment); // Gọi hàm insert

            // Nếu thành công thì update bảng Order
            if ("00".equals(payment.getResponseCode()) && "00".equals(payment.getTransactionStatus())) {
                orderRepository.updatePaymentStatus(orderId, "PAID");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu Database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}