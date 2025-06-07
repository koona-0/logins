package site.login.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//사용자 정보 수정 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {
 
 @NotBlank(message = "이름은 필수입니다.")
 @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
 private String name;
 
 @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하여야 합니다.")
 private String password; // 비밀번호는 선택사항 (소셜 로그인 사용자는 변경 불가)
}