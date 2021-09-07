package com.myretail.restful.config;

import com.myretail.restful.domain.entity.Price;
import com.myretail.restful.repository.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

@Slf4j
@Configuration
public class MongoConfig {
    @Autowired
    private PriceRepository priceRepository;

    @PostConstruct
    public void initData() {
        Price price = new Price(new Long(13860428), new BigDecimal(13.49).setScale(2, BigDecimal.ROUND_HALF_UP), "USD");
        priceRepository.save(price);
        Price price1 = new Price(new Long(54456119), new BigDecimal(16.99).setScale(2, BigDecimal.ROUND_HALF_UP), "USD");
        priceRepository.save(price1);
        Price price2 = new Price(new Long(13264003), new BigDecimal(6.49).setScale(2, BigDecimal.ROUND_HALF_UP), "USD");
        priceRepository.save(price2);
        Price price3 = new Price(new Long(12954218), new BigDecimal(8.00).setScale(2, BigDecimal.ROUND_HALF_UP), "USD");
        priceRepository.save(price3);
        log.info("+++++++++++++++++++++Initialized the DB+++++++++++++++++++++");
    }
}
