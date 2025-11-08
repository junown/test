package com.example.testapi;

// 🚨 필요한 import 문 전체 목록
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashMap;
import java.util.Map;

@Configuration
// 🚨 @EnableWebSecurity는 삭제합니다! (자동 설정 충돌 방지)
public class SecurityConfig {

    // 3단계에서 만든 CustomOAuth2UserService를 주입받습니다.
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    // --- Bean 1: SecurityFilterChain (메인 보안 설정) ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2AuthorizationRequestResolver customResolver) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // (A) 스코프 주입을 위한 'customResolver' 사용
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(customResolver)
                        )
                        // (B) 'content' 파싱을 위한 'customOAuth2UserService' 사용
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                );

        return http.build();
    }

    // --- Bean 2: OAuth2AuthorizationRequestResolver (스코프 문제 해결) ---
    // (람다 대신 익명 클래스 사용)
    @Bean
    public OAuth2AuthorizationRequestResolver customResolver(ClientRegistrationRepository repo) {

        final DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest defaultRequest = defaultResolver.resolve(request);
                return modifyChzzkRequest(defaultRequest);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                OAuth2AuthorizationRequest defaultRequest = defaultResolver.resolve(request, clientRegistrationId);
                return modifyChzzkRequest(defaultRequest);
            }

            // 스코프를 추가하는 공통 로직
            private OAuth2AuthorizationRequest modifyChzzkRequest(OAuth2AuthorizationRequest defaultRequest) {
                if (defaultRequest == null) {
                    return null;
                }

                String registrationId = defaultRequest.getAttribute("registrationId");
                if ("chzzk".equals(registrationId)) {
                    Map<String, Object> additionalParams = new HashMap<>(defaultRequest.getAdditionalParameters());

                    // (scope 추가)
                    additionalParams.put("scope", "유저 조회");

                    return OAuth2AuthorizationRequest.from(defaultRequest)
                            .additionalParameters(additionalParams)
                            .build();
                }
                return defaultRequest;
            }
        };
    }

    // --- Bean 3: ClientRegistrationRepository (치지직 API 공식 URI 설정) ---
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration chzzkRegistration = ClientRegistration
                .withRegistrationId("chzzk") // 👈 (중요) "chzzk"
                .clientId("7118939e-061b-4ad3-b4b3-bb0cb2432931") // 본인 ID

                // 🚨🚨🚨 반드시 재발급 받은 새 Secret Key로 교체!!! 🚨🚨🚨
                .clientSecret("oO5iKLLqYNRWe69Q8GV9DtPNYQphPuXx21Px1ckd1jc")

                .redirectUri("http://localhost:8001/login/oauth2/code/chzzk")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientName("Chzzk")

                // (scope는 customResolver가 처리하므로 여기서 삭제)

                // (1) 공식 문서 기준 '인증' URI
                .authorizationUri("https://chzzk.naver.com/account-interlock")

                // (2) 공식 문서 기준 '토큰' URI
                .tokenUri("https://openapi.chzzk.naver.com/auth/v1/token")

                // (3) 사용자 정보 URI (이건 원래 맞았음)
                .userInfoUri("https://openapi.chzzk.naver.com/open/v1/users/me")

                // (4) CustomUserService에서 사용할 ID 필드 이름
                .userNameAttributeName("channelId")
                .build();

        return new InMemoryClientRegistrationRepository(chzzkRegistration);
    }
}