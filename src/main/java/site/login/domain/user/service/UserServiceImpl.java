package site.login.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import site.login.domain.user.dto.UserInfoDto;
import site.login.domain.user.dto.UserUpdateDto;
import site.login.domain.user.entity.User;
import site.login.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserInfoDto getCurrentUser(String email) {
        log.info("사용자 정보 조회: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        return UserInfoDto.from(user);
    }

    @Override
    @Transactional
    public UserInfoDto updateUser(String email, UserUpdateDto userUpdateDto) {
        log.info("사용자 정보 수정: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        // 이름 수정
        if (StringUtils.hasText(userUpdateDto.getName())) {
            user.updateName(userUpdateDto.getName());
        }
        
        // 비밀번호 수정 (소셜 로그인 사용자는 비밀번호 변경 불가)
        if (StringUtils.hasText(userUpdateDto.getPassword()) && !user.isSocialUser()) {
            String encodedPassword = passwordEncoder.encode(userUpdateDto.getPassword());
            user.updatePassword(encodedPassword);
        }
        
        User updatedUser = userRepository.save(user);
        log.info("사용자 정보 수정 완료: {}", email);
        
        return UserInfoDto.from(updatedUser);
    }
}