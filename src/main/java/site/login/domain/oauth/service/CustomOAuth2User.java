package site.login.domain.oauth.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import site.login.domain.user.entity.User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().getKey())
        );
    }

    @Override
    public String getName() {
        return user.getEmail(); // OAuth2User의 name으로 이메일 사용
    }

    // 추가 편의 메서드들
    public String getEmail() {
        return user.getEmail();
    }

    public String getUserName() {
        return user.getName();
    }

    public String getProvider() {
        return user.getProvider();
    }

    public Long getUserId() {
        return user.getId();
    }
}