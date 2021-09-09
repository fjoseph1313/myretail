package com.myretail.restful.service;

import com.myretail.restful.domain.PriceDto;
import com.myretail.restful.domain.ProductDto;
import com.myretail.restful.domain.entity.Price;
import com.myretail.restful.exception.NoSqlException;
import com.myretail.restful.exception.RedSkyException;
import com.myretail.restful.exception.ValidationException;
import com.myretail.restful.repository.PriceRepository;
import com.myretail.restful.util.RedisServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static org.springframework.beans.BeanUtils.copyProperties;

@Slf4j
@Component
public class MyRetailService {
    /**
     * Avoiding the real hostname: https://redsky.target.com use localhost instead.
     */
    public String redSkyProductUrl = "http://localhost:8088/v3/pdp/tcin/{productId}?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics&key=candidate";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PriceRepository priceRepository;
    @Autowired
    private RedisServiceUtil redisServiceUtil;

    public ProductDto getProductById(Long productId) throws Exception {
        log.info("Retrieving product information from NoSQL DB and RedSky service..");
        ProductDto productDto = new ProductDto();
        productDto.setId(productId);
        productDto.setName(getProductName(productId));
        productDto.setCurrent_price(getProductPrice(productId));
        return productDto;
    }

    public ProductDto updateProductPriceById(Long productId, ProductDto productDto) {
        log.info("Updating product with id: {} with new price: {}", productId, productDto.getCurrent_price().getValue());
        PriceDto existingPrice = getProductPrice(productId);
        if (existingPrice.getValue().compareTo(productDto.getCurrent_price().getValue()) == 0) {
            String responseMessage = "Price hasn't changed.. can't update product's price.";
            log.info(responseMessage);
            throw new ValidationException(responseMessage);
        }
        Price currentPrice = priceRepository.findById(productId).get();
        currentPrice.setValue(productDto.getCurrent_price().getValue());
        currentPrice.setCurrency_code(productDto.getCurrent_price().getCurrency_code());
        priceRepository.save(currentPrice);
        log.info("Successfully updated Product with id: {} with new price: value: {} currency_code: {}"
                , productId, currentPrice.getValue(), currentPrice.getCurrency_code());
        //improvement. do not return the request object. contract a new object with valid values.
        //may call GET call again here.
        return productDto;
    }

    /**
     * Using a given productId, fetch product price from a NoSQL datastore. in this case - Embedded MongoDB
     *
     * @param productId
     * @return productPrice
     */
    public PriceDto getProductPrice(Long productId) {
        Price currentPrice = priceRepository.findById(productId).orElse(null);
        if (Objects.nonNull(currentPrice)) {
            PriceDto priceDto = new PriceDto();
            copyProperties(currentPrice, priceDto);
            return priceDto;
        } else {
            String response = String.format("Exception from Database: No Price exist for this product: %s", productId);
            log.info(response);
            throw new NoSqlException(response);
        }

    }

    /**
     * Using a given ProductId, fetch product name from external API - Target RedSkyService.
     * In this case we are using embedded wiremock server to simulate the API calls to RedSky API
     * For performance "improvement and High availability", Redis cache is used to store ProductName
     * so as to avoid expensive call to RedSky Server
     *
     * @param productId
     * @return ProductName. (assuming the response is sanitized to String value)
     */
    public String getProductName(Long productId) {
        try {
            String cacheKey = redisServiceUtil.getProductKey(productId);
            String productName = redisServiceUtil.getValue(cacheKey);
            if (Objects.nonNull(productName)) {
                log.info("EXIST: Product name exists in the REDIS cache.. RedSky API call is skipped...");
                return productName;
            }
            String getUrl = redSkyProductUrl.replace("{productId}", productId.toString());
            ResponseEntity<String> response = restTemplate.getForEntity(getUrl, String.class);

            redisServiceUtil.setValue(cacheKey, response.getBody());
            log.info("NON-EXIST: Product name did not exist in the REDIS cache.. RedSky API was called.");
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            String response = String
                    .format("Exception from RedSky external API: Couldn't retrieve Product Item:: {} - {}",
                            ex.getRawStatusCode(), ex.getStatusText());
            log.error(response);
            throw new RedSkyException(response);
        }
    }
}
