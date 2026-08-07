package com.finance.smartLedger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

  @Bean
  public OpenAPI smartLedgerOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Smart Ledger API")
                .description(
                    "RESTful API for SmartLedger - A comprehensive financial management system for educational institutions")
                .version("1.0.0")
                .contact(
                    new Contact()
                        .name("SmartLedger Team")
                        .email("support@smartledger.com")
                        .url("https://smartledger.com"))
                .license(
                    new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
        .components(
            new Components()
                .addSecuritySchemes(
                    SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token for authentication. Format: Bearer <token>")));
  }
}
