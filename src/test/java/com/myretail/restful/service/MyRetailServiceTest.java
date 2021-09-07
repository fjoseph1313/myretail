package com.myretail.restful.service;

import com.myretail.restful.domain.PriceDto;
import com.myretail.restful.domain.ProductDto;
import com.myretail.restful.domain.entity.Price;
import com.myretail.restful.exception.NoSqlException;
import com.myretail.restful.exception.RedSkyException;
import com.myretail.restful.exception.ValidationException;
import com.myretail.restful.repository.PriceRepository;
import com.myretail.restful.util.RedisServiceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MyRetailServiceTest {

    @InjectMocks
    private MyRetailService myRetailService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private PriceRepository priceRepository;
    @Mock
    private RedisServiceUtil redisServiceUtil;

    private ResponseEntity<String> redSkyResponse;
    private Optional<Price> existingPrice;
    private Price price;
    private ProductDto productRequest;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(myRetailService, "redSkyProductUrl", "redSkyService.com/url");
        redSkyResponse = ResponseEntity.ok("ProductName From RedSky");
        price = new Price(new Long(13860428),
                new BigDecimal("13.49"), "USD");
        existingPrice = Optional.of(price);
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
    @DisplayName("FETCH SUCCESS: Fetching product by ID, Happy Path")
    public void testGetProductWithIdSuccess() throws Exception {
        //Given
        when(restTemplate.getForEntity(anyString(), any(Class.class))).thenReturn(redSkyResponse);
        when(priceRepository.findById(new Long(13860428))).thenReturn(existingPrice);
        //When
        ProductDto product = myRetailService.getProductById(new Long(13860428));
        //Then
        assertNotNull(product);
        assertEquals("ProductName From RedSky", product.getName());
        assertEquals(new Long("13860428"), product.getId());
        assertEquals(new BigDecimal("13.49"), product.getCurrent_price().getValue());
        verify(restTemplate, times(1)).getForEntity(anyString(), any(Class.class));
        verify(priceRepository, times(1)).findById(new Long(13860428));
        verify(redisServiceUtil, times(1)).setValue(any(), any());
    }

    @Test
    @DisplayName("NOSQL FAILURE: Price not available")
    public void testGetProductPriceNotFound() throws Exception {
        when(redisServiceUtil.getValue(any())).thenReturn("ProductName From RedSky");
        assertThrows(NoSqlException.class, () -> {
            myRetailService.getProductById(new Long(13860428));
        });
        verify(priceRepository, times(1)).findById(new Long(13860428));
    }

    @Test
    @DisplayName("REDSKY FAILURE: Price not available")
    public void testGetProductRedSkyFailure() throws Exception {
        doThrow(HttpClientErrorException.class).when(restTemplate).getForEntity(anyString(), any(Class.class));
        assertThrows(RedSkyException.class, () -> {
            myRetailService.getProductById(new Long(13860428));
        });
    }

    @Test
    @DisplayName("UPDATE SUCCESS: Price updated with new Price values")
    public void testUpdateProductByIdSuccess() {
        //Given
        when(priceRepository.findById(new Long(13860428))).thenReturn(existingPrice);
        //When
        ProductDto updatedProduct = myRetailService.updateProductPriceById(new Long(13860428), productRequest);
        //Then
        Price updatedPrice = new Price(new Long(13860428),
                new BigDecimal("1200"), "CAN");
        verify(priceRepository, times(1)).save(updatedPrice);
        assertEquals(new BigDecimal("1200"), updatedProduct.getCurrent_price().getValue());
        assertEquals("CAN", updatedProduct.getCurrent_price().getCurrency_code());
    }

    @Test
    @DisplayName("UPDATE FAILURE: No Price change")
    public void testUpdateFailure() throws Exception {
        productRequest.setId(new Long(138604281));
        productRequest.getCurrent_price().setValue(new BigDecimal("13.49")); //same price as existing price
        when(priceRepository.findById(new Long(13860428))).thenReturn(existingPrice);

        assertThrows(ValidationException.class, () -> {
            myRetailService.updateProductPriceById(new Long(13860428), productRequest);
        });
        verify(priceRepository, times(0)).save(any(Price.class));
    }
}
