package com.myretail.restful.controller;

import com.myretail.restful.domain.PriceDto;
import com.myretail.restful.domain.ProductDto;
import com.myretail.restful.service.MyRetailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MyRetailControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @MockBean
    private MyRetailService myRetailService;
    private ProductDto productRequest;
    private String testUrl;

    @BeforeEach
    public void setup() {
        testUrl = "/my-retail/products/13860428";
        setProductRequest();
    }

    public void setProductRequest() {
        productRequest = new ProductDto();
        productRequest.setId(new Long(13860428));
        PriceDto newPrice = new PriceDto();
        newPrice.setValue(new BigDecimal("1200"));
        newPrice.setCurrency_code("CAN");
        productRequest.setCurrent_price(newPrice);
    }

    @Test
    public void testUpdateProductValidationFailure() throws Exception {
        testUrl = "/my-retail/products/138604281";
        HttpEntity<ProductDto> request = new HttpEntity<>(productRequest);
        ResponseEntity<String> response = testRestTemplate.exchange(testUrl, HttpMethod.PUT, request, String.class);
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Only update the same Product.. IDs don't match.", response.getBody());
    }

    @Test
    public void testUpdateProductNoPriceForUpdate() throws Exception {
        productRequest.setCurrent_price(null);
        HttpEntity<ProductDto> request = new HttpEntity<>(productRequest);
        ResponseEntity<String> response = testRestTemplate.exchange(testUrl, HttpMethod.PUT, request, String.class);
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Must provide currency values: {currency_value, currency_code}", response.getBody());
    }

    @Test
    public void testUpdateProductSuccess() {
        HttpEntity<ProductDto> request = new HttpEntity<>(productRequest);
        when(myRetailService.updateProductPriceById(new Long(13860428), productRequest)).thenReturn(productRequest);
        ResponseEntity<ProductDto> response = testRestTemplate.exchange(testUrl, HttpMethod.PUT, request, ProductDto.class);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testGetProductByIdSuccess() throws Exception {
        when(myRetailService.getProductById(new Long(13860428))).thenReturn(productRequest);
        ResponseEntity<ProductDto> response = testRestTemplate.getForEntity(testUrl, ProductDto.class);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }
}
