package com.bookverse.bookCatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class BookCatalogApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookCatalogApplication.class, args);
	}

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		
		// Add Hibernate6 module to handle lazy loading properly
		Hibernate6Module hibernate6Module = new Hibernate6Module();
		hibernate6Module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
		hibernate6Module.configure(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION, false);
		hibernate6Module.configure(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);
		
		mapper.registerModule(hibernate6Module);
		
		// Add JavaTimeModule to handle LocalDateTime and other Java 8 time types
		mapper.registerModule(new JavaTimeModule());
		
		return mapper;
	}
}
