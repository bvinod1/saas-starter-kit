package com.saasstarterkit.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.keycloak.adapters.springboot.KeycloakAutoConfiguration;

@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@org.springframework.boot.autoconfigure.EnableAutoConfiguration(exclude = {})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
} 