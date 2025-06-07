package site.login.domain.user.repository;

import site.login.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);
    
    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);
    
    // 소셜 로그인 사용자 찾기 (추후 사용)
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}