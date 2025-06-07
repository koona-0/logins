package site.login.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import site.login.domain.oauth.service.CustomOAuth2UserService;
import site.login.global.jwt.JwtAuthenticationEntryPoint;
import site.login.global.jwt.JwtAuthenticationFilter;
import site.login.global.oauth2.OAuth2AuthenticationFailureHandler;
import site.login.global.oauth2.OAuth2AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class TempSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용)
            .csrf(csrf -> csrf.disable())
            
            // 세션 사용 안함 (JWT 사용)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 인증 실패 시 처리
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
            
            // URL별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 URL
                .requestMatchers("/api/auth/**").permitAll()              // 일반 로그인/회원가입
                .requestMatchers("/oauth2/**").permitAll()                // OAuth2 관련 모든 경로
                .requestMatchers("/login/oauth2/**").permitAll()          // OAuth2 콜백 경로
                .requestMatchers("/oauth2/authorization/**").permitAll()   // OAuth2 인증 시작 경로
                .requestMatchers("/").permitAll()                         // 메인 페이지
                .requestMatchers("/error").permitAll()                    // 에러 페이지
                .requestMatchers("/favicon.ico").permitAll()              // 파비콘
                .requestMatchers("/api/debug/**").permitAll()             // 디버그 API (개발용)
                
                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )
            
            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService) // 커스텀 OAuth2 사용자 서비스
                )
                .successHandler(oAuth2AuthenticationSuccessHandler) // 성공 핸들러
                .failureHandler(oAuth2AuthenticationFailureHandler) // 실패 핸들러
            )
            
            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}