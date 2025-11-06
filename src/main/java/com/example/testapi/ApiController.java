package com.example.testapi;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class ApiController {

    private final WebClient webClient;

    public ApiController(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * '유저 정보 조회' API 호출
     */
    @GetMapping("/api/my-info") // (이 주소는 우리가 정하기 나름입니다)
    public Mono<String> getMyInfo(
            @RegisteredOAuth2AuthorizedClient("chzzk") OAuth2AuthorizedClient authorizedClient
    ) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        // ⬇️ 문서에 나온 엔드포인트(/open/v1/users/me)로 수정
        return webClient.get()
                .uri("/open/v1/users/me")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class);
    }
}