package com.liveseat.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI liveSeatOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LiveSeat API")
                        .description("공연 예매 시스템 REST API 문서")
                        .version("1.0.0"));
    }
}
