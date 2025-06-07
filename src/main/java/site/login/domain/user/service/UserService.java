package site.login.domain.user.service;

import site.login.domain.user.dto.UserInfoDto;
import site.login.domain.user.dto.UserUpdateDto;

public interface UserService {
    
    // 현재 로그인한 사용자 정보 조회
    UserInfoDto getCurrentUser(String email);
    
    // 사용자 정보 수정
    UserInfoDto updateUser(String email, UserUpdateDto userUpdateDto);
}