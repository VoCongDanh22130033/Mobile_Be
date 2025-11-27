package com.shopsense.dto;


import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

// Giả định sử dụng Lombok (như trong mã gốc)
@Getter
@Setter
public class PaymentResDTO implements Serializable {
    private String status;
    private String message;
    private String URL;
}