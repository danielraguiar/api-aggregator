package com.kenect.api_aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class ApiAggregatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiAggregatorApplication.class, args);
	}

}
