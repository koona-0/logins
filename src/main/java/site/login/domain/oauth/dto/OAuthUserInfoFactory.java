package site.login.domain.oauth.dto;

import java.util.Map;

public class OAuthUserInfoFactory {

    public static OAuthUserInfo getOAuthUserInfo(String provider, Map<String, Object> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            throw new IllegalArgumentException("사용자 정보가 비어있습니다.");
        }
        
        try {
            switch (provider.toLowerCase()) {
                case "google":
                    return new GoogleUserInfo(attributes);
                case "kakao":
                    return new KakaoUserInfo(attributes);
                case "naver":
                    return new NaverUserInfo(attributes);
                default:
                    throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + provider);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("사용자 정보 파싱 중 오류 발생: " + e.getMessage(), e);
        }
    }
}