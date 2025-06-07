package site.login.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = true) // 소셜 로그인 사용자는 이메일이 없을 수 있음 (카카오 제외)
    private String email;
    
    @Column(nullable = true) // 소셜 로그인 사용자는 비밀번호가 없음
    private String password;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
    
    // 소셜 로그인 관련 필드
    @Column
    private String provider; // google, kakao, naver
    
    @Column
    private String providerId; // 소셜 로그인 고유 ID
    
    // 추가 정보 입력 완료 여부
    @Builder.Default
    @Column(nullable = false)
    private Boolean profileCompleted = false;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // 비밀번호 업데이트 메서드
    public void updatePassword(String password) {
        this.password = password;
    }
    
    // 이름 업데이트 메서드
    public void updateName(String name) {
        this.name = name;
    }
    
    // 이메일 업데이트 메서드 (소셜 로그인 사용자용)
    public void updateEmail(String email) {
        this.email = email;
    }
    
    // 프로필 완성 처리
    public void completeProfile() {
        this.profileCompleted = true;
    }
    
    // 소셜 로그인 사용자 여부 확인
    public boolean isSocialUser() {
        return provider != null && providerId != null;
    }
    
    // 이메일이 없는 소셜 사용자 여부 확인 (카카오는 이제 항상 이메일이 있어야 함)
    public boolean needsEmailSetup() {
        return isSocialUser() && (email == null || email.isEmpty()) && !"kakao".equals(provider);
    }
    
    // 카카오 사용자 여부 확인
    public boolean isKakaoUser() {
        return "kakao".equals(provider);
    }
    
    // 네이버 사용자 여부 확인
    public boolean isNaverUser() {
        return "naver".equals(provider);
    }
    
    // 구글 사용자 여부 확인
    public boolean isGoogleUser() {
        return "google".equals(provider);
    }
}