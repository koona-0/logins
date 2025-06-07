package site.login.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.login.domain.user.entity.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

// 사용자 정보 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDto {
    private Long id;
    private String email;
    private String name;
    private String role;
    private LocalDateTime createdAt;
    private boolean isSocialUser;
    
    // Entity -> DTO 변환
    public static UserInfoDto from(User user) {
        return UserInfoDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().getKey())
                .createdAt(user.getCreatedAt())
                .isSocialUser(user.isSocialUser())
                .build();
    }
}