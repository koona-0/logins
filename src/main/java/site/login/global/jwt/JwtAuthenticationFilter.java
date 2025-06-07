package site.login.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import site.login.domain.user.entity.User;
import site.login.domain.user.repository.UserRepository;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // OAuth2 관련 경로는 JWT 검증 제외
        String requestURI = request.getRequestURI();
        if (shouldSkipFilter(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 1. 헤더에서 JWT 토큰 추출
            String token = getTokenFromRequest(request);
            
            // 2. 토큰이 있고 유효한지 확인
            if (token != null && jwtUtil.validateToken(token)) {
                
                // 3. 토큰에서 이메일 추출
                String email = jwtUtil.getEmailFromToken(token);
                
                // 4. 이메일로 사용자 정보 조회
                User user = userRepository.findByEmail(email)
                        .orElse(null);
                
                if (user != null) {
                    // 5. Spring Security 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                    user.getEmail(), // principal (주체)
                                    null,           // credentials (자격증명)
                                    List.of(new SimpleGrantedAuthority(user.getRole().getKey())) // authorities (권한)
                            );
                    
                    // 6. 요청 정보 설정
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 7. SecurityContext에 인증 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("JWT 인증 성공: {}", email);
                }
            }
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생", e);
            // 인증 실패 시 SecurityContext 클리어
            SecurityContextHolder.clearContext();
        }
        
        // 8. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * JWT 필터를 건너뛸 경로 확인
     */
    private boolean shouldSkipFilter(String requestURI) {
        return requestURI.startsWith("/oauth2/") ||
               requestURI.startsWith("/login/oauth2/") ||
               requestURI.startsWith("/api/auth/") ||
               requestURI.startsWith("/api/debug/") ||
               requestURI.equals("/") ||
               requestURI.equals("/error") ||
               requestURI.equals("/favicon.ico");
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰 추출
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거
        }
        
        return null;
    }
}