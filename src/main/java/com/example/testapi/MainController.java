package com.example.testapi;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    /**
     * 1. 첫 홈페이지 (로그인 페이지)
     * SecurityConfig에서 .requestMatchers("/", "/login").permitAll() 로 설정했기 때문에
     * 로그인하지 않아도 이 페이지는 보입니다.
     */
    @GetMapping("/")
    public String index() {
        return "index"; // (resources/templates/index.html)
    }

    /**
     * 2. 로그인 성공 후 메인 페이지
     * SecurityConfig에서 .anyRequest().authenticated() 로 설정했기 때문에
     * 로그인을 해야만 이 페이지로 올 수 있습니다.
     *
     * @AuthenticationPrincipal OAuth2User user: Spring Security가 세션에서
     * 로그인한 사용자 정보를 꺼내서 user 객체에 담아줍니다.
     */
    @GetMapping("/main")
    public String main(@AuthenticationPrincipal OAuth2User user, Model model) {

        // CustomOAuth2UserService에서 우리가 "content" 맵을 기준으로
        // user 객체를 만들었기 때문에, "channelId" 등을 바로 꺼낼 수 있습니다.
        String channelId = user.getAttribute("channelId");
        String channelName = user.getAttribute("channelName");

        model.addAttribute("channelId", channelId);
        model.addAttribute("channelName", channelName);

        return "main"; // (resources/templates/main.html)
    }
}