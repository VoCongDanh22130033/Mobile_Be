package com.shopsense.controller;

import com.shopsense.service.PaymentService;
import jakarta.servlet.http.HttpServletResponse; // Dùng javax.servlet... nếu bản cũ
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Map;

@RestController
public class VNPayController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/vnpay_return")
    public void vnpayReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        System.out.println("--------------------------------------------------");
        System.out.println("!!! ĐÃ NHẬN ĐƯỢC CALLBACK TỪ VNPAY !!!");
        System.out.println("Params: " + params.toString());
        System.out.println("--------------------------------------------------");

        // 1. GỌI SERVICE ĐỂ LƯU DATABASE
        paymentService.processVnPayReturn(params);

        // 2. Xây dựng Deep Link để mở lại App Flutter
        MultiValueMap<String, String> multiMap = new LinkedMultiValueMap<>();
        params.forEach((k, v) -> multiMap.add(k, v));

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("myshopsense://vnpay_return")
                .queryParams(multiMap);

        String deepLinkUri = uriBuilder.toUriString();

        // 3. Thực hiện Redirect chuẩn bằng HttpServletResponse
        response.sendRedirect(deepLinkUri);
    }
}