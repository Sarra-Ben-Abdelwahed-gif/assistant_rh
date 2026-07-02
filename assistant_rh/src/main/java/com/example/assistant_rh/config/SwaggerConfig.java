package com.example.assistant_rh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Intelligent HR Assistant — API")
                .version("1.0.0")
                .description("""
                    Comprehensive API documentation for the Intelligent HR Assistant Platform
                    powered by Gemini AI and Model Context Protocol (MCP).
                    
                    **Available Roles:**
                    - `HR_ADMIN` → Full structural and administrative access
                    - `EMPLOYEE` → Limited functional access
                    - `CANDIDATE` → Public access
                    
                    **Authentication Strategy:**
                    Use the `/api/auth/login` endpoint to retrieve a valid JWT token,
                    then click the **Authorize** button and input `Bearer YOUR_TOKEN`.
                    """)
                .contact(new Contact()
                    .name("Proxym IT")
                    .email("contact@proxym.com"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://proxym.com")))
            .addSecurityItem(
                new SecurityRequirement()
                    .addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes(
                    "Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Provide your active JWT bearer token to access secured endpoints.")));
    }
}
