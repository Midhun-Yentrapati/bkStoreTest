package com.bookstore.user_authentication_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import com.bookstore.user_authentication_service.config.JwtProperties;

@SpringBootApplication(scanBasePackages = "com.bookstore.user_authentication_service")
@EnableDiscoveryClient
@EnableFeignClients
@EnableConfigurationProperties(JwtProperties.class)
public class UserAuthenticationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserAuthenticationServiceApplication.class, args);
	}

}
