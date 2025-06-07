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
public class OAuth2DebugController {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/oauth2-clients")
    public Map<String, Object> getOAuth2Clients() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 등록된 OAuth2 클라이언트들 확인
            String[] providers = {"google", "kakao", "naver"};
            
            for (String provider : providers) {
                try {
                    ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(provider);
                    if (registration != null) {
                        Map<String, Object> clientInfo = new HashMap<>();
                        clientInfo.put("registrationId", registration.getRegistrationId());
                        clientInfo.put("clientId", registration.getClientId());
                        clientInfo.put("authorizationUri", registration.getProviderDetails().getAuthorizationUri());
                        clientInfo.put("tokenUri", registration.getProviderDetails().getTokenUri());
                        clientInfo.put("userInfoUri", registration.getProviderDetails().getUserInfoEndpoint().getUri());
                        clientInfo.put("redirectUri", registration.getRedirectUri());
                        clientInfo.put("scopes", registration.getScopes());
                        
                        result.put(provider, clientInfo);
                        log.info("OAuth2 클라이언트 정보 - {}: {}", provider, clientInfo);
                    } else {
                        result.put(provider, "등록되지 않음");
                        log.warn("OAuth2 클라이언트 없음: {}", provider);
                    }
                } catch (Exception e) {
                    result.put(provider, "오류: " + e.getMessage());
                    log.error("OAuth2 클라이언트 조회 오류 - {}: {}", provider, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("OAuth2 디버그 중 전체 오류", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}