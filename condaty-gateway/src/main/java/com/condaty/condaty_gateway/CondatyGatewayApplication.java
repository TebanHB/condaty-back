package com.condaty.condaty_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CondatyGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(CondatyGatewayApplication.class, args);
	}

}
