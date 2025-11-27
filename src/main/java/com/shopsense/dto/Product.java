package com.shopsense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private int id;
    private String title;
    private String description;
    private String salePrice;
    private String regularPrice;
    private String thumbnailUrl;
    private Category category;
    private int categoryId;
}
