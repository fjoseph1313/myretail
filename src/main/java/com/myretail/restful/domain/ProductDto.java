package com.myretail.restful.domain;

import lombok.Data;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private PriceDto current_price;
}
