package com.shopsense.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import org.springframework.web.util.UriComponentsBuilder;
// THÊM 2 DÒNG IMPORT NÀY
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RestController
public class VNPayController {

    // CHÚ Ý: Đổi kiểu trả về từ ResponseEntity<?> sang String
    @GetMapping("/vnpay_return")
    public String vnpayReturn(@RequestParam Map<String, String> params) {

        // 1. In ra toàn bộ tham số để kiểm tra
        System.out.println("VNPAY RETURN PARAMS: " + params);

        // 2. CHUYỂN ĐỔI MAP TIÊU CHUẨN SANG MULTIVALUEMAP (FIX LỖI BIÊN DỊCH)
        MultiValueMap<String, String> multiMap = new LinkedMultiValueMap<>();
        params.forEach((k, v) -> multiMap.add(k, v)); // Thêm từng cặp key-value

        // 3. Xây dựng Deep Link URI
        // Sử dụng MultiValueMap đã chuyển đổi
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("myshopsense://vnpay_return")
                .queryParams(multiMap); // Sử dụng multiMap đã chuyển đổi

        String deepLinkUri = uriBuilder.toUriString();

        // 4. Trả về lệnh chuyển hướng (REDIRECT)
        return "redirect:" + deepLinkUri;
    }
}