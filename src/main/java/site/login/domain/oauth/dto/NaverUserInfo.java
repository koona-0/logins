package site.login.domain.oauth.dto;

import java.util.Map;

public class NaverUserInfo extends OAuthUserInfo {

    public NaverUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        Map<String, Object> response = getResponseMap();
        if (response != null) {
            Object id = response.get("id");
            if (id != null) {
                return String.valueOf(id);
            }
        }
        return "naver_unknown_id";
    }

    @Override
    public String getName() {
        Map<String, Object> response = getResponseMap();
        if (response != null) {
            // 1순위: name
            String name = (String) response.get("name");
            if (name != null && !name.trim().isEmpty()) {
                return name.trim();
            }
            
            // 2순위: nickname
            String nickname = (String) response.get("nickname");
            if (nickname != null && !nickname.trim().isEmpty()) {
                return nickname.trim();
            }
        }
        return "네이버사용자"; // 기본값
    }

    @Override
    public String getEmail() {
        Map<String, Object> response = getResponseMap();
        if (response != null) {
            String email = (String) response.get("email");
            if (email != null && !email.trim().isEmpty()) {
                return email.trim();
            }
        }
        // 네이버에서 이메일을 가져올 수 없는 경우 빈 값 반환 (카카오와 동일하게 처리)
        return "";
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    // 네이버 응답에서 response 객체 추출 (안전한 캐스팅)
    private Map<String, Object> getResponseMap() {
        try {
            if (attributes != null && attributes.containsKey("response")) {
                Object responseObj = attributes.get("response");
                if (responseObj instanceof Map) {
                    return (Map<String, Object>) responseObj;
                }
            }
        } catch (Exception e) {
            // 캐스팅 오류 시 null 반환
            System.err.println("네이버 response 파싱 오류: " + e.getMessage());
        }
        return null;
    }
}