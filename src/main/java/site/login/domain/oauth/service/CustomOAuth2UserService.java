package site.login.domain.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.login.domain.oauth.dto.OAuthUserInfo;
import site.login.domain.oauth.dto.OAuthUserInfoFactory;
import site.login.domain.user.entity.Role;
import site.login.domain.user.entity.User;
import site.login.domain.user.repository.UserRepository;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            // 기본 OAuth2UserService를 사용하여 OAuth2User 정보 가져오기
            OAuth2User oauth2User = super.loadUser(userRequest);
            
            String provider = userRequest.getClientRegistration().getRegistrationId();
            String userNameAttributeName = userRequest.getClientRegistration()
                    .getProviderDetails()
                    .getUserInfoEndpoint()
                    .getUserNameAttributeName();
                    
            log.info("OAuth2 로그인 시도: {}", provider);
            log.info("UserNameAttributeName: {}", userNameAttributeName);
            log.debug("OAuth2 사용자 정보: {}", oauth2User.getAttributes());
            
            Map<String, Object> attributes = oauth2User.getAttributes();
            
            // principalName 확인 및 기본값 설정
            Object principalNameValue = attributes.get(userNameAttributeName);
            if (principalNameValue == null || principalNameValue.toString().trim().isEmpty()) {
                log.warn("PrincipalName이 비어있음. 기본값으로 'id' 속성 사용");
                principalNameValue = attributes.get("id");
                if (principalNameValue == null) {
                    // 최후의 수단: 임시 ID 생성
                    principalNameValue = "temp_" + System.currentTimeMillis();
                    log.warn("ID 속성도 없음. 임시 ID 생성: {}", principalNameValue);
                }
                userNameAttributeName = "id";
            }
            
            // 제공자별 사용자 정보 추출
            OAuthUserInfo oAuthUserInfo;
            try {
                oAuthUserInfo = OAuthUserInfoFactory.getOAuthUserInfo(provider, attributes);
            } catch (Exception e) {
                log.error("OAuth2 사용자 정보 추출 실패 - Provider: {}, Error: {}", provider, e.getMessage());
                throw new OAuth2AuthenticationException("사용자 정보를 가져올 수 없습니다: " + e.getMessage());
            }
            
            log.info("추출된 정보 - Provider: {}, ID: {}, 이름: {}, 이메일: {}", 
                    provider, oAuthUserInfo.getId(), oAuthUserInfo.getName(), 
                    oAuthUserInfo.getEmail() != null && !oAuthUserInfo.getEmail().isEmpty() ? oAuthUserInfo.getEmail() : "없음");
            
            // 사용자 저장 또는 업데이트
            User user = saveOrUpdateUser(oAuthUserInfo);
            
            // 권한 설정
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
            
            // DefaultOAuth2User로 반환 (principalName 보장)
            return new DefaultOAuth2User(
                    Collections.singleton(authority),
                    attributes,
                    userNameAttributeName
            );
            
        } catch (OAuth2AuthenticationException e) {
            // OAuth2AuthenticationException은 그대로 던짐
            throw e;
        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 예상치 못한 오류 발생", e);
            throw new OAuth2AuthenticationException("로그인 처리 중 오류가 발생했습니다.");
        }
    }

    private User saveOrUpdateUser(OAuthUserInfo oAuthUserInfo) {
        // providerId가 null이거나 빈 문자열인 경우 처리
        String providerId = oAuthUserInfo.getId();
        String provider = oAuthUserInfo.getProvider();
        
        if (providerId == null || providerId.trim().isEmpty()) {
            log.error("Provider ID가 비어있음: {}", oAuthUserInfo);
            throw new OAuth2AuthenticationException("사용자 식별값을 가져올 수 없습니다.");
        }
        
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);

        User user;
        if (existingUser.isPresent()) {
            // 기존 사용자 정보 업데이트
            user = existingUser.get();
            user.updateName(oAuthUserInfo.getName());
            
            // 기존 사용자가 이메일이 없고 새로 받은 이메일이 있는 경우 업데이트
            String newEmail = oAuthUserInfo.getEmail();
            if ((user.getEmail() == null || user.getEmail().isEmpty()) && 
                newEmail != null && !newEmail.trim().isEmpty()) {
                user.updateEmail(newEmail);
                user.completeProfile(); // 이메일이 추가되면 프로필 완성
                log.info("기존 사용자 이메일 업데이트: {}", newEmail);
            }
            
            log.info("기존 소셜 로그인 사용자 정보 업데이트: {}", 
                    user.getEmail() != null ? user.getEmail() : "이메일 없음");
        } else {
            // 새 소셜 로그인 사용자 생성
            String email = oAuthUserInfo.getEmail();
            
            // 카카오는 이제 이메일이 필수이므로 반드시 있어야 함
            if ("kakao".equals(provider) && (email == null || email.trim().isEmpty())) {
                log.error("카카오 로그인에서 이메일 정보를 받을 수 없음. Attributes: {}", oAuthUserInfo);
                throw new OAuth2AuthenticationException("카카오 로그인에서 이메일 정보를 가져올 수 없습니다. 카카오 앱 설정을 확인해주세요.");
            }
            
            // 이메일이 있는 경우 중복 체크
            if (email != null && !email.trim().isEmpty()) {
                Optional<User> emailUser = userRepository.findByEmail(email);
                if (emailUser.isPresent()) {
                    log.warn("이미 존재하는 이메일로 소셜 로그인 시도: {}", email);
                    throw new OAuth2AuthenticationException("이미 존재하는 이메일입니다. 일반 로그인을 사용해주세요.");
                }
            } else {
                // 다른 제공자(네이버, 구글 등)에서 이메일이 없는 경우에만 null 허용
                if (!"kakao".equals(provider)) {
                    email = null;
                    log.info("이메일 정보 없는 소셜 로그인 ({}) - 나중에 추가 정보 입력 필요", provider);
                }
            }
            
            // 사용자 이름이 비어있는 경우 기본값 설정
            String userName = oAuthUserInfo.getName();
            if (userName == null || userName.trim().isEmpty()) {
                userName = "사용자_" + providerId.substring(0, Math.min(providerId.length(), 8));
                log.warn("사용자 이름이 비어있음. 기본값 설정: {}", userName);
            }
            
            // 프로필 완성 여부 결정
            boolean isProfileComplete = email != null && !email.trim().isEmpty();
            
            user = User.builder()
                    .email(email) // 카카오는 필수, 다른 제공자는 선택적
                    .name(userName)
                    .role(Role.USER)
                    .provider(provider)
                    .providerId(providerId)
                    .password("") // 소셜 로그인 사용자는 비밀번호 없음
                    .profileCompleted(isProfileComplete) // 이메일이 있으면 완성, 없으면 미완성
                    .build();
            
            log.info("새 소셜 로그인 사용자 생성: {} ({}), 이메일: {}, 프로필완성: {}", 
                    user.getName(), user.getProvider(), 
                    user.getEmail() != null ? user.getEmail() : "없음",
                    user.getProfileCompleted());
        }

        return userRepository.save(user);
    }
}