package com.shopsense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private int id;
    private String title;
    private String description;
    private String icon;
    private Integer parentId;

}
