package site.login.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.login.domain.auth.dto.LoginRequestDto;
import site.login.domain.auth.dto.LoginResponseDto;
import site.login.domain.auth.dto.SignUpRequestDto;
import site.login.domain.user.entity.Role;
import site.login.domain.user.entity.User;
import site.login.domain.user.repository.UserRepository;
import site.login.global.jwt.JwtUtil;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입
     */
    @Override
    @Transactional
    public void signUp(SignUpRequestDto signUpRequestDto) {
        log.info("회원가입 시도: {}", signUpRequestDto.getEmail());
        
        // 1. 이메일 중복 확인
        if (userRepository.existsByEmail(signUpRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        
        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signUpRequestDto.getPassword());
        
        // 3. User 엔티티 생성
        User user = User.builder()
                .email(signUpRequestDto.getEmail())
                .password(encodedPassword)
                .name(signUpRequestDto.getName())
                .role(Role.USER)
                .build();
        
        // 4. 사용자 저장
        userRepository.save(user);
        log.info("회원가입 완료: {}", signUpRequestDto.getEmail());
    }

    /**
     * 로그인
     */
    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("로그인 시도: {}", loginRequestDto.getEmail());
        
        // 1. 이메일로 사용자 찾기
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
        
        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        
        // 3. JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getEmail());
        
        log.info("로그인 성공: {}", loginRequestDto.getEmail());
        
        // 4. 응답 DTO 생성
        return LoginResponseDto.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().getKey())
                .build();
    }
}