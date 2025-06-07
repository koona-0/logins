package site.login.domain.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.login.domain.user.dto.UserInfoDto;
import site.login.domain.user.dto.UserUpdateDto;
import site.login.domain.user.service.UserService;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 현재 로그인한 사용자 정보 조회 (마이페이지)
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getCurrentUser(Authentication authentication) {
        try {
            String email = authentication.getName(); // JWT에서 추출한 이메일
            UserInfoDto userInfo = userService.getCurrentUser(email);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자 정보 수정
     */
    @PutMapping("/me")
    public ResponseEntity<UserInfoDto> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        try {
            String email = authentication.getName();
            UserInfoDto updatedUserInfo = userService.updateUser(email, userUpdateDto);
            return ResponseEntity.ok(updatedUserInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("사용자 정보 수정 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자 정보 조회 테스트 (JWT 토큰 필요)
     */
    @GetMapping("/test")
    public ResponseEntity<String> testAuth(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok("인증된 사용자: " + email);
    }
}