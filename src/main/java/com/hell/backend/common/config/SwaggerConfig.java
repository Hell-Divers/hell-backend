package com.hell.backend.common.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI OpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }

    //swagger 페이지 정보
    private Info apiInfo() {
        return new Info()
                .title("Hell-Divers Swagger API ")
                .description("API documentation for Hell-Divers project!")
                .version("1.0");
    }
}
