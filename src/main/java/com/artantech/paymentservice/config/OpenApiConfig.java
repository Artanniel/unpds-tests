package com.artantech.paymentservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI paymentServiceOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Payment Service API")
                                                .description("API RESTful de Processamento de Pagamentos construída com Spring Boot seguindo TDD.")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Elias Nogueira / Team")
                                                                .email("contact@paymentservice.com")))
                                .servers(List.of(
                                                new Server().url("http://localhost:8080")
                                                                .description("Servidor Local de Desenvolvimento")));
        }
}
