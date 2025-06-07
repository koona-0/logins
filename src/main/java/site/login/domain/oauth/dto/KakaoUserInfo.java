package site.login.domain.oauth.dto;

import java.util.Map;

public class KakaoUserInfo extends OAuthUserInfo {

    public KakaoUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        if (account != null) {
            Map<String, Object> profile = (Map<String, Object>) account.get("profile");
            if (profile != null) {
                String nickname = (String) profile.get("nickname");
                if (nickname != null && !nickname.isEmpty()) {
                    return nickname;
                }
            }
        }
        return "카카오사용자"; // 기본값
    }

    @Override
    public String getEmail() {
        // 카카오 응답 구조: kakao_account.email
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            return (String) kakaoAccount.get("email");
        }
        return null;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }
}