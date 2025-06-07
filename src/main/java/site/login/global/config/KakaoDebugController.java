package site.login.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/debug")
public class KakaoDebugController {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/kakao-config")
    public Map<String, Object> getKakaoConfig() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ClientRegistration kakaoRegistration = clientRegistrationRepository.findByRegistrationId("kakao");
            
            if (kakaoRegistration != null) {
                result.put("registrationId", kakaoRegistration.getRegistrationId());
                result.put("clientId", kakaoRegistration.getClientId());
                result.put("hasClientSecret", kakaoRegistration.getClientSecret() != null && !kakaoRegistration.getClientSecret().isEmpty());
                result.put("clientAuthenticationMethod", kakaoRegistration.getClientAuthenticationMethod().getValue());
                result.put("authorizationGrantType", kakaoRegistration.getAuthorizationGrantType().getValue());
                result.put("redirectUri", kakaoRegistration.getRedirectUri());
                result.put("scopes", kakaoRegistration.getScopes());
                result.put("authorizationUri", kakaoRegistration.getProviderDetails().getAuthorizationUri());
                result.put("tokenUri", kakaoRegistration.getProviderDetails().getTokenUri());
                result.put("userInfoUri", kakaoRegistration.getProviderDetails().getUserInfoEndpoint().getUri());
                result.put("userNameAttributeName", kakaoRegistration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName());
                
                log.info("카카오 OAuth2 설정: {}", result);
            } else {
                result.put("error", "카카오 클라이언트 등록이 없습니다.");
                log.error("카카오 OAuth2 클라이언트가 등록되지 않았습니다.");
            }
            
        } catch (Exception e) {
            result.put("error", "카카오 설정 조회 중 오류: " + e.getMessage());
            log.error("카카오 OAuth2 설정 조회 오류", e);
        }
        
        return result;
    }
}