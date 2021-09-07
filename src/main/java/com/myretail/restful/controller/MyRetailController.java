package com.myretail.restful.controller;

import com.myretail.restful.domain.ProductDto;
import com.myretail.restful.exception.ValidationException;
import com.myretail.restful.service.MyRetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/my-retail")
public class MyRetailController {

    @Autowired
    private MyRetailService myRetailService;

    @GetMapping(value = "/products/{productId}")
    public ResponseEntity<Object> getProduct(@PathVariable Long productId) {
        try {
            log.info("Retrieving a product with id: {}", productId);
            ProductDto productDto = myRetailService.getProductById(new Long(productId));
            return ResponseEntity.ok().body(productDto);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }

    }

    @PutMapping(value = "/products/{productId}")
    public ResponseEntity<Object> updateProductPrice(@PathVariable Long productId, @RequestBody ProductDto product) {
        try {
            validateInput(product, productId);
            ProductDto updatedProduct = myRetailService.updateProductPriceById(productId, product);
            return ResponseEntity.ok().body(updatedProduct);
        } catch (ValidationException ve) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ve.getMessage());
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    private void validateInput(ProductDto product, Long productId) {
        String errorMessage = null;
        if (!productId.equals(product.getId())) {
            errorMessage = "Only update the same Product.. IDs don't match.";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (Objects.isNull(product.getCurrent_price())
                || Objects.isNull(product.getCurrent_price().getValue())
                || Objects.isNull(product.getCurrent_price().getCurrency_code())) {
            errorMessage = "Must provide currency values: {currency_value, currency_code}";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }
}
