package com.myretail.restful.repository;

import com.myretail.restful.domain.entity.Price;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PriceRepository extends MongoRepository<Price, Long> {
}
