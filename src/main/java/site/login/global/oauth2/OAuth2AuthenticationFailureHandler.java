package site.login.global.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException; // OAuth2AuthenticationException 임포트
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      AuthenticationException exception) throws IOException, ServletException {
        
        String errorMessage = "로그인 중 오류가 발생했습니다."; // 기본 오류 메시지
        
        // OAuth2 관련 상세 에러 처리
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            // getDescription()이 null일 수도 있으므로 방어 코드 추가
            errorMessage = oauth2Exception.getError().getDescription() != null && !oauth2Exception.getError().getDescription().isEmpty() 
                           ? oauth2Exception.getError().getDescription() 
                           : "OAuth2 로그인 처리 중 알 수 없는 오류 발생.";
            
            log.error("OAuth2 로그인 실패 - 에러코드: {}, 설명: {}", 
                    oauth2Exception.getError().getErrorCode(), 
                    oauth2Exception.getError().getDescription());
        } else {
            // 일반 AuthenticationException 처리
            // getMessage()가 null일 수도 있으므로 방어 코드 추가
            errorMessage = exception.getMessage() != null && !exception.getMessage().isEmpty()
                           ? exception.getMessage()
                           : "일반 로그인 처리 중 알 수 없는 오류 발생.";
            log.error("일반 인증 실패: {}", exception.getMessage());
        }
        
        // 요청 정보 로깅 (디버깅용)
        log.debug("실패한 요청 URI: {}", request.getRequestURI());
        log.debug("실패한 요청 파라미터: {}", request.getQueryString());
        
        // 프론트엔드로 에러와 함께 리다이렉트
        // errorMessage는 위에서 null이 아니도록 처리했으므로 바로 사용 가능
        String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/login")
                .queryParam("error", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
                .build()
                .toUriString();
        
        // SimpleUrlAuthenticationFailureHandler의 기본 리다이렉트 전략 사용
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}