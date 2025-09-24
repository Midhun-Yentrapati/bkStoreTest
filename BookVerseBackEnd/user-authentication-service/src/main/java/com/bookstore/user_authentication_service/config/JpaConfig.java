package com.bookstore.user_authentication_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.bookstore.user_authentication_service.repository")
@EnableJpaAuditing
public class JpaConfig {
}
