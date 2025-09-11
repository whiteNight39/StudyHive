package com.studyhive.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fredrick 🖤 | StudyHive API Documentation")
                        .description("Comprehensive documentation for StudyHive's backend.")
                        .version("1.0.0"));
    }
}