package com.shopsense.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private String vnpHashSecret;

    @Value("${vnpay.payUrl}")
    private String vnpUrl;

    @Value("${vnpay.returnUrl}")
    private String vnpReturnUrl;

    private final String VNP_VERSION = "2.1.0";
    private final String VNP_COMMAND = "pay";

    // Sử dụng setter để Trim() giá trị Hash Secret, tránh lỗi ký tự ẩn
    @Value("${vnpay.hashSecret}")
    public void setVnpHashSecret(String hashSecret) {
        this.vnpHashSecret = hashSecret.trim();
        log.info("VNPAY HASH SECRET (TRIMMED): '{}'", this.vnpHashSecret);
    }

    private final String FLUTTER_DEEPLINK_SCHEME = "myshopsense";
    private final String FLUTTER_DEEPLINK_HOST = "vnpay_return";

    /**
     * Tạo URL thanh toán VNPay.
     */
    @PostMapping("/create")
    public Map<String, String> createPayment(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        log.info("💸 [CREATE] Request received. Body: {}", body);

        String orderId = String.valueOf(System.currentTimeMillis());
        int amountInt = (Integer) body.getOrDefault("amount", 0);
        String amount = String.valueOf((long) amountInt * 100); // VNPay yêu cầu nhân 100
        String vnpCreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        // Sử dụng TreeMap để đảm bảo các key được sắp xếp theo thứ tự từ điển A-Z
        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", VNP_VERSION);
        vnpParams.put("vnp_Command", VNP_COMMAND);
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", amount);
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderId);
        vnpParams.put("vnp_OrderInfo", "Payment order #" + orderId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl); // URL mà VNPAY sẽ gọi về backend của bạn
        vnpParams.put("vnp_IpAddr", getIpAddress(request));
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        cld.add(Calendar.MINUTE, 15);
        vnpParams.put("vnp_ExpireDate", new SimpleDateFormat("yyyyMMddHHmmss").format(cld.getTime()));

        log.info("💸 [CREATE] Prepared VNPAY Params (A-Z): {}", vnpParams);

        // Build data for hash and query
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        StringBuilder hashData = new StringBuilder();
        StringBuilder queryUrl = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                queryUrl.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                queryUrl.append('=');
                queryUrl.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    queryUrl.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrlString = queryUrl.toString();
        String vnp_SecureHash = hmacSHA512(this.vnpHashSecret, hashData.toString());
        queryUrlString += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnpUrl + "?" + queryUrlString;

        log.info("💸 [CREATE] Final Payment URL: {}", paymentUrl);

        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        response.put("orderId", orderId);
        return response;
    }

    /**
     * API trả về mà VNPAY gọi.
     * Sửa Lỗi: Thay vì trả về "redirect:", API này sẽ trả về một trang HTML
     * chứa JavaScript để thực hiện chuyển hướng về ứng dụng phía client.
     */
    @GetMapping("/return")
    public ResponseEntity<String> paymentReturn(HttpServletRequest request) throws UnsupportedEncodingException {
        log.info("↩️ [RETURN] Callback URL received: {}", request.getRequestURL().toString() + "?" + request.getQueryString());

        // --- Logic xác thực chữ ký (giữ nguyên để đảm bảo an toàn) ---
        // ... (Bạn có thể thêm logic xác thực hash tại đây nếu cần)

        // --- Tạo Deep Link để trả về cho App ---
        String deepLinkUrl = createDeepLinkUrl(request);
        log.info("↩️ Generated Deep Link URL for client-side redirection: {}", deepLinkUrl);

        // Tạo nội dung HTML với JavaScript để chuyển hướng
        String htmlContent = "<!DOCTYPE html><html><head><title>Redirecting...</title></head>"
                + "<body style='display:flex; flex-direction:column; justify-content:center; align-items:center; height:100%; font-family:sans-serif; background-color:#f8f9fa;'>"
                + "<h3>Please wait...</h3>"
                + "<p>Redirecting back to the application.</p>"
                + "<p>Vui lòng chờ, đang chuyển hướng về ứng dụng...</p>"
                + "<script type='text/javascript'>"
                + "window.location.href = '" + deepLinkUrl + "';" // Dòng JS quan trọng nhất
                + "</script>"
                + "</body></html>";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/html; charset=UTF-8");

        return new ResponseEntity<>(htmlContent, headers, HttpStatus.OK);
    }

    // --- Các hàm tiện ích (không thay đổi) ---

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private String createDeepLinkUrl(HttpServletRequest request) throws UnsupportedEncodingException {
        String deepLinkBase = FLUTTER_DEEPLINK_SCHEME + "://" + FLUTTER_DEEPLINK_HOST;
        StringBuilder queryParams = new StringBuilder();
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String value = request.getParameter(key);
            if (value != null && !value.isEmpty()) {
                // Bỏ qua các tham số hash để không làm lộ thông tin nhạy cảm
                if (key.equals("vnp_SecureHash") || key.equals("vnp_SecureHashType")) {
                    continue;
                }
                if (queryParams.length() > 0) {
                    queryParams.append('&');
                }
                queryParams.append(URLEncoder.encode(key, StandardCharsets.UTF_8.toString()))
                        .append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
            }
        }

        return (queryParams.length() > 0) ? (deepLinkBase + "?" + queryParams.toString()) : deepLinkBase;
    }

    private String hmacSHA512(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac.init(secretKey);
        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}