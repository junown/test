package com.example.testapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // 1. 루트(/)와 로그인 관련 경로는 누구나 접근 허용
                        .requestMatchers("/", "/login/**", "/oauth2/**").permitAll()
                        // 2. 그 외 /api/** 같은 모든 요청은 인증(로그인)을 요구
                        .anyRequest().authenticated()
                )
                // 3. OAuth 2.0 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/") // 4. 우리가 만든 로그인 페이지(index.html) 경로
                        .defaultSuccessUrl("/") // 5. 로그인 성공 시 돌아올 페이지
                )
                // 6. 로그아웃 설정
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }
}