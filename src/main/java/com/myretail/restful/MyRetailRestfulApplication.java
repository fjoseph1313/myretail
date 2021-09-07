package com.myretail.restful;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@ComponentScan
@EnableMongoRepositories(basePackages = "com.myretail.restful.repository")
@SpringBootApplication
public class MyRetailRestfulApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyRetailRestfulApplication.class, args);
	}

}
