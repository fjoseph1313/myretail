package com.myretail.restful.domain.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document
public class Price {
    @Id
    private Long productId;
    private BigDecimal value;
    private String currency_code;

    public Price(Long productId, BigDecimal value, String currency_code) {
        this.productId = productId;
        this.value = value;
        this.currency_code = currency_code;
    }
}
