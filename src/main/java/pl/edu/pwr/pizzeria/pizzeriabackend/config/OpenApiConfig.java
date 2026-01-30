package pl.edu.pwr.pizzeria.pizzeriabackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pizzeriaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pizzeria Backend API")
                        .version("1.0.0")
                        .description("REST API for Pizzeria Management System. " +
                                "This API provides endpoints for managing orders, payments, deliveries, " +
                                "products, menus, promotions, employees, ingredients, and inventory."))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from /api/auth/login endpoint. " +
                                        "Format: Bearer <token>")));
    }
}

