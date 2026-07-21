package com.stockmaster.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Point d'entrée de l'application StockMaster CM.
 *
 * <p>Spring Boot scanne automatiquement tous les packages
 * {@code com.stockmaster.*} pour détecter les beans, entités et repositories
 * de l'ensemble des modules du monolithe modulaire.</p>
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.stockmaster")
@EntityScan(basePackages = "com.stockmaster")
@EnableJpaRepositories(basePackages = "com.stockmaster")
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
@ConfigurationPropertiesScan(basePackages = "com.stockmaster")
public class StockMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockMasterApplication.class, args);
    }
}
