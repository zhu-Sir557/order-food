package com.restaurant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Restaurant Order Food System - Application Entry Point.
 *
 * <p>Excludes Spring Security auto-configuration since we only use
 * BCryptPasswordEncoder bean via manual configuration.</p>
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@MapperScan("com.restaurant.mapper")
@EnableScheduling
public class RestaurantApplication {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(RestaurantApplication.class, args);
    }
}
