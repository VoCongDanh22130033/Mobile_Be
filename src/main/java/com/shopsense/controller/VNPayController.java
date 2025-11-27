package com.shopsense.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VNPayController {

    @GetMapping("/vnpay_return")
    public ResponseEntity<?> vnpayReturn(@RequestParam Map<String, String> params) {

        // In ra toàn bộ tham số để kiểm tra
        System.out.println("VNPAY RETURN PARAMS: " + params);

        return ResponseEntity.ok(params);
    }
}
