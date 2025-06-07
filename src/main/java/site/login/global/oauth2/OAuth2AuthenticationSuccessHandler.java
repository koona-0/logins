package site.login.global.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import site.login.domain.user.entity.User;
import site.login.domain.user.repository.UserRepository;
import site.login.global.jwt.JwtUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        try {
            // OAuth2User로 캐스팅 (DefaultOAuth2User는 OAuth2User 구현체)
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            
            // Provider 정보 추출
            String provider = extractProvider(request);
            String providerId = extractProviderId(oauth2User, provider);
            
            log.info("OAuth2 로그인 성공 - Provider: {}, ProviderId: {}", provider, providerId);
            
            // 데이터베이스에서 사용자 조회
            Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, providerId);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                log.info("OAuth2 로그인 성공: {} ({})", 
                        user.getEmail() != null ? user.getEmail() : "이메일 없음", user.getProvider());
                
                // JWT 토큰 생성 (이메일이 없으면 providerId 사용)
                String tokenSubject = user.getEmail() != null && !user.getEmail().isEmpty() 
                        ? user.getEmail() 
                        : user.getProvider() + "_" + user.getProviderId();
                        
                String token = jwtUtil.generateToken(tokenSubject);
                
                // 프론트엔드로 리다이렉트 (토큰과 사용자 정보 포함)
                String redirectUrl = createRedirectUrl(token, user);
                
                log.info("OAuth2 로그인 완료 - 리다이렉트: {}", redirectUrl);
                
                // 캐시 방지 헤더 추가
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
                
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                
            } else {
                log.error("사용자를 찾을 수 없음 - Provider: {}, ProviderId: {}", provider, providerId);
                String errorUrl = "http://localhost:3000/oauth2/redirect?error=user_not_found";
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
            }
            
        } catch (Exception e) {
            log.error("OAuth2 로그인 성공 처리 중 오류 발생", e);
            String errorUrl = "http://localhost:3000/oauth2/redirect?error=internal_error";
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    private String createRedirectUrl(String token, User user) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
                .queryParam("token", token)
                .queryParam("name", URLEncoder.encode(user.getName(), StandardCharsets.UTF_8))
                .queryParam("role", "ROLE_" + user.getRole().name())
                .queryParam("provider", user.getProvider())
                .queryParam("timestamp", System.currentTimeMillis()); // 캐시 방지
        
        // 이메일이 있는 경우에만 추가
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            builder.queryParam("email", URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8));
            builder.queryParam("needsEmailSetup", "false");
        } else {
            // 이메일이 없는 경우 (카카오 무료 버전)
            builder.queryParam("needsEmailSetup", "true");
        }
        
        return builder.build().toUriString();
    }
    
    private String extractProvider(HttpServletRequest request) {
        // URL에서 provider 추출 (/login/oauth2/code/kakao -> kakao)
        String requestUri = request.getRequestURI();
        if (requestUri.contains("/kakao")) {
            return "kakao";
        } else if (requestUri.contains("/naver")) {
            return "naver";
        } else if (requestUri.contains("/google")) {
            return "google";
        }
        throw new RuntimeException("알 수 없는 OAuth2 Provider입니다: " + requestUri);
    }
    
    private String extractProviderId(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        switch (provider.toLowerCase()) {
            case "kakao":
                // 카카오는 id 필드 사용
                Object kakaoId = attributes.get("id");
                if (kakaoId != null) {
                    return kakaoId.toString();
                }
                break;
                
            case "naver":
                // 네이버는 response.id 구조 사용
                Object response = attributes.get("response");
                if (response instanceof Map) {
                    Map<String, Object> responseMap = (Map<String, Object>) response;
                    Object naverId = responseMap.get("id");
                    if (naverId != null) {
                        return naverId.toString();
                    }
                }
                break;
                
            case "google":
                // 구글은 sub 필드 사용
                Object googleSub = attributes.get("sub");
                if (googleSub != null) {
                    return googleSub.toString();
                }
                break;
        }
        
        // 기본적으로 OAuth2User의 getName() 사용 (principal name)
        String name = oauth2User.getName();
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        
        throw new RuntimeException("Provider ID를 추출할 수 없습니다 - Provider: " + provider + ", Attributes: " + attributes);
    }
}