package site.login.domain.auth.service;

import site.login.domain.auth.dto.SignUpRequestDto;
import site.login.domain.auth.dto.LoginRequestDto;
import site.login.domain.auth.dto.LoginResponseDto;

public interface AuthService {
    
    // 회원가입
    void signUp(SignUpRequestDto signUpRequestDto);
    
    // 로그인
    LoginResponseDto login(LoginRequestDto loginRequestDto);
}