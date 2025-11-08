package com.example.testapi;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 OAuth2 유저 정보를 불러옵니다.
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. "chzzk"인지 확인
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if (registrationId.equals("chzzk")) {
            // 3. 치지직의 비표준 응답에서 "content" 객체를 추출합니다.
            Map<String, Object> attributes = oAuth2User.getAttributes();

            // "content" 키가 있는지, Map 타입인지 확인
            if (attributes.containsKey("content") && attributes.get("content") instanceof Map) {

                @SuppressWarnings("unchecked")
                Map<String, Object> contentAttributes = (Map<String, Object>) attributes.get("content");

                // 4. "channelId"를 유저의 ID(Name)로 사용합니다.
                String userNameAttributeName = userRequest.getClientRegistration()
                        .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName(); // "channelId"

                // 5. "content" 맵을 기반으로 새로운 OAuth2User 객체를 생성하여 반환합니다.
                //    이렇게 해야 Spring Security가 "channelId" 등을 올바르게 인식합니다.
                return new DefaultOAuth2User(
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                        contentAttributes, // 👈 핵심: 전체 attributes 대신 'content' 맵을 사용
                        userNameAttributeName
                );
            } else {
                // "content" 키가 없거나 형식이 맞지 않는 경우 예외 처리
                throw new OAuth2AuthenticationException("Invalid user info response from Chzzk");
            }
        }

        // chzzk가 아니면(예: 구글, 네이버) 원래대로 반환
        return oAuth2User;
    }
}