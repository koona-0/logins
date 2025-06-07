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
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
    
    // 소셜 로그인 관련 필드 (추후 사용)
    @Column
    private String provider; // google, kakao, naver
    
    @Column
    private String providerId; // 소셜 로그인 고유 ID
    
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
    
    // 소셜 로그인 사용자 여부 확인
    public boolean isSocialUser() {
        return provider != null && providerId != null;
    }
}