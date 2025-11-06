package com.example.testapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration // 👈 @Configuration 확인
public class AppConfig {

    @Bean // 👈 @Bean 확인
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                // 🚨 로그인 API용 기본 주소로 설정
                .baseUrl("https://openapi.chzzk.naver.com")
                .build();
    }
}