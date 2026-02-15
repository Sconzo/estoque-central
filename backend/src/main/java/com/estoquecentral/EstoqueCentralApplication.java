package com.estoquecentral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Estoque Central - Sistema ERP Omnichannel Brasileiro
 *
 * Main application entry point.
 * Uses Spring Modulith for bounded context separation via packages.
 */
@SpringBootApplication
public class EstoqueCentralApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstoqueCentralApplication.class, args);
    }
}
