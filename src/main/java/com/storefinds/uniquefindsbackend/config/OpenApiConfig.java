package com.storefinds.uniquefindsbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-10
     * Purpose: Build OpenAPI metadata and JWT bearer security scheme for Swagger UI and Apifox export.
     * Params: None
     * Returns:
     * - OpenAPI: configured OpenAPI bean
     * Throws: None
     */
    @Bean
    public OpenAPI uniqueFindsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Unique Finds Backend API")
                        .description("OpenAPI document for Unique Finds backend services.")
                        .version("v1")
                        .contact(new Contact().name("Unique Finds Backend Team")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}
