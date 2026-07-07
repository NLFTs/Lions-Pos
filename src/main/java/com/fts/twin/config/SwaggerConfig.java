package com.fts.twin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration with Bearer JWT security scheme.
 */
@Configuration
public class SwaggerConfig {
    @Value("${app.name:spravel}")
    private String appName;

    @Value("${app.version:0.0.1}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        String title = appName.substring(0, 1).toUpperCase() + appName.substring(1) + " API";

        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description("REST API documentation for " + title)
                        .version("v" + appVersion)
                        .contact(new Contact()
                                .name("Spravel Team")
                                .email("admin@spravel.com")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authorization header using the Bearer scheme. "
                                                + "Enter your access token without the 'Bearer ' prefix.")))
                .security(List.of(
                        new SecurityRequirement().addList("bearer-jwt")));
    }
}
