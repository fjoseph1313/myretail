package com.myretail.restful.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceDto {
    private BigDecimal value;
    private String currency_code;
}
