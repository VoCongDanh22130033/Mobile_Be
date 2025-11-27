package com.shopsense.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    // --- Cấu hình VNPay từ application.properties ---
    @Value("${vnpay.tmnCode}")
    private String vnpTmnCode;

    // SỬA: Khai báo và sử dụng setter để đảm bảo trim()
    private String vnpHashSecret;

    @Value("${vnpay.payUrl}")
    private String vnpUrl;

    @Value("${vnpay.returnUrl}")
    private String vnpReturnUrl;

    // Tích hợp hằng số từ Config.java
    private final String VNP_VERSION = "2.1.0";
    private final String VNP_COMMAND = "pay";


    // Khởi tạo setter để TRIM Hash Secret
    @Value("${vnpay.hashSecret}")
    public void setVnpHashSecret(String hashSecret) {
        // VNPAY Hash Secret thường chứa ký tự ẩn/khoảng trắng, cần phải trim()
        this.vnpHashSecret = hashSecret.trim();
        log.info("VNPAY HASH SECRET (TRIMMED): {}", this.vnpHashSecret);
    }

    private final String FLUTTER_DEEPLINK_SCHEME = "myshopsense";
    private final String FLUTTER_DEEPLINK_HOST = "vnpay_return";

    // 1. Phương thức tạo payment URL
    @PostMapping("/create")
    public Map<String, String> createPayment(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {

        // LOG: Ghi lại dữ liệu request đầu vào
        log.info("💸 [CREATE] Request received. Body: {}", body);

        // Tên tham số theo quy tắc VNPAY (vd: vnp_TxnRef, vnp_Amount, ...)
        String orderId = String.valueOf(System.currentTimeMillis());

        // VNPay yêu cầu số tiền nhân với 100
        int amountInt = (Integer) body.getOrDefault("amount", 0);
        String amount = String.valueOf((long) amountInt * 100);

        String vnpCreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        // Sử dụng TreeMap để đảm bảo các key được sắp xếp theo thứ tự từ điển A-Z
        Map<String, String> vnpParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        vnpParams.put("vnp_Version", VNP_VERSION);
        vnpParams.put("vnp_Command", VNP_COMMAND);
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", amount);
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderId);

        String orderInfo = "Payment order #" + orderId + " - " + body.getOrDefault("orderDescription", "");
        String encodedOrderInfo = URLEncoder.encode(orderInfo, StandardCharsets.UTF_8.toString()); // Encode OrderInfo
        vnpParams.put("vnp_OrderInfo", encodedOrderInfo);

        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        // Bổ sung các trường bắt buộc thiếu
        String ipAddr = getIpAddress(request);
        vnpParams.put("vnp_IpAddr", ipAddr);
        vnpParams.put("vnp_OrderType", "other");

        // Tính Expire Date (15 phút)
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = new SimpleDateFormat("yyyyMMddHHmmss").format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnp_ExpireDate);

        // LOG: Ghi lại các tham số VNPAY đã chuẩn bị
        log.info("💸 [CREATE] Prepared VNPAY Params (A-Z): {}", vnpParams);

        StringBuilder hashData = new StringBuilder();
        StringBuilder queryUrl = new StringBuilder();

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {

                // Giá trị dùng cho Hash Data phải được URL Encode (UTF-8)
                String encodedForHash = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString());
                hashData.append(fieldName).append('=').append(encodedForHash);

                // Giá trị dùng cho Query String phải được URL Encode (UTF-8)
                queryUrl.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()))
                        .append('=').append(encodedForHash); // Dùng cùng giá trị đã encoded

                if (itr.hasNext()) {
                    queryUrl.append('&');
                    hashData.append('&');
                }
            }
        }

        log.warn("VNPAY DEBUG - HASH DATA INPUT (URL Encoded): {}", hashData.toString());

        // 3. Tạo chữ ký HMAC SHA512
        String secureHash = hmacSHA512(this.vnpHashSecret, hashData.toString());
        queryUrl.append("&vnp_SecureHash=").append(secureHash);

        log.warn("VNPAY DEBUG - SECURE HASH OUTPUT: {}", secureHash);

        String paymentUrl = vnpUrl + "?" + queryUrl.toString();

        // LOG: Ghi lại URL thanh toán cuối cùng
        log.info("💸 [CREATE] Final Payment URL: {}", paymentUrl);


        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        response.put("orderId", orderId);
        return response;
    }

    // 2. Phương thức Callback sau khi VNPay gọi đến Backend
    @GetMapping("/return")
    public String paymentReturn(HttpServletRequest request) throws Exception {

        // LOG: Ghi lại URL Callback
        log.info("↩️ [RETURN] Callback URL received: {}", request.getRequestURL().toString() + "?" + request.getQueryString());

        Map<String, String[]> params = request.getParameterMap();
        Map<String, String> vnpParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (String key : params.keySet()) {
            vnpParams.put(key, params.get(key)[0]);
        }

        // LOG: Ghi lại tất cả các tham số VNPay gửi về
        log.info("↩️ [RETURN] All VNPAY Params: {}", vnpParams);


        String secureHash = vnpParams.get("vnp_SecureHash");

        // Loại bỏ tham số Hash khỏi TreeMap trước khi tạo CheckSum
        vnpParams.remove("vnp_SecureHash");
        vnpParams.remove("vnp_SecureHashType");

        StringBuilder hashData = new StringBuilder();

        // 1. Lặp qua các tham số đã được sắp xếp (A-Z) để tạo CheckSum
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Iterator<String> itr = fieldNames.iterator();

        while(itr.hasNext()) {
            String fieldName = itr.next();
            String value = vnpParams.get(fieldName);

            if (value != null && !value.isEmpty()) {
                String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());

                hashData.append(fieldName).append('=').append(encodedValue);
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String checkSum = hmacSHA512(this.vnpHashSecret, hashData.toString());

        log.warn("VNPAY RETURN DEBUG - CHECK SUM DATA: {}", hashData.toString());
        log.warn("VNPAY RETURN DEBUG - CHECK SUM: {}", checkSum);
        log.warn("VNPAY RETURN DEBUG - SECURE HASH (Received): {}", secureHash);

        // ... (Logic kiểm tra Hash và Redirect)

        // 2. Tạo URL Deep Link để Redirect
        String deepLinkUrl = createDeepLinkUrl(request);

        // 3. Kiểm tra Hash và Redirect
        if (secureHash != null && secureHash.equalsIgnoreCase(checkSum)) {
            log.info("✅ Xác thực Hash thành công. Chuyển hướng về App. VNPAY_CODE: {}", vnpParams.get("vnp_ResponseCode"));
        } else {
            log.error("❌ Xác thực Hash thất bại! CheckSum: {}, SecureHash: {}", checkSum, secureHash);
        }

        // Luôn Redirect về Deep Link để Flutter xử lý kết quả
        log.info("↩️ Redirecting to Deep Link: {}", deepLinkUrl);
        return "redirect:" + deepLinkUrl;
    }

    // Hàm lấy IP Address (Được tích hợp từ Config.java)
    private String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        return ipAdress;
    }

    // Phương thức tạo Deep Link URL (Không thay đổi, đảm bảo encoding)
    private String createDeepLinkUrl(HttpServletRequest request) throws UnsupportedEncodingException {
        String deepLinkBase = FLUTTER_DEEPLINK_SCHEME + "://" + FLUTTER_DEEPLINK_HOST;

        StringBuilder queryParams = new StringBuilder();
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String value = request.getParameter(key);

            if (value != null && !value.isEmpty()) {

                // Loại bỏ vnp_SecureHash và vnp_SecureHashType khỏi Deep Link
                if (key.equals("vnp_SecureHash") || key.equals("vnp_SecureHashType")) {
                    continue;
                }

                if (queryParams.length() > 0) {
                    queryParams.append('&');
                }

                // Dùng URLEncoder.encode cho KEY và VALUE để đảm bảo an toàn
                queryParams.append(URLEncoder.encode(key, "UTF-8"))
                        .append('=')
                        .append(URLEncoder.encode(value, "UTF-8"));
            }
        }

        if (queryParams.length() > 0) {
            return deepLinkBase + "?" + queryParams.toString();
        }
        return deepLinkBase;
    }

    // Hàm tạo HMAC SHA512 (Được tích hợp từ Config.java và đảm bảo dùng UTF-8)
    public static String hmacSHA512(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac.init(secretKey);
        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) result.append(String.format("%02x", b));
        return result.toString();
    }
}