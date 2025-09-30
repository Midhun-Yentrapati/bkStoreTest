package com.bookverse.CartOrderManagement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// Import for CorsFilter and related classes should be removed if no longer used

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@EnableDiscoveryClient
@EnableConfigurationProperties
public class CartOrderManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(CartOrderManagementApplication.class, args);
	}

	
}