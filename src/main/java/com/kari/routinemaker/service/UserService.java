package com.kari.routinemaker.service;

import com.kari.routinemaker.domain.User;
import com.kari.routinemaker.dto.UserRequestDto;
import com.kari.routinemaker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화 인코더 주입

    //application.properties에서 가져옴
    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;

    /**
     * 회원가입 로직 (비밀번호 암호화 적용)
     */
    @Transactional
    public Long signup(UserRequestDto.SignupRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 2. 비밀번호 암호화 (BCrypt 방식)
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. User 엔티티 생성 (암호화된 비밀번호 주입)
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword) // 평문 대신 암호화된 패스워드 저장
                .name(request.getName())
                .nickname(request.getNickname())
                .provider("LOCAL")
                .build();

        // 4. DB에 저장 후 생성된 PK(id) 반환
        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    /**
     * 로그인 로직
     */
    public Long login(UserRequestDto.LoginRequest request) {
        // 1. 이메일 존재 여부 확인
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 2. 비밀번호 일치 여부 확인 (평문 vs 암호화된 패스워드 비교)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 로그인 성공 시 유저의 PK(id) 반환 (나중에 세션이나 토큰 대용으로 활용 가능)
        return user.getId();
    }

    /**
     * 카카오 소셜 로그인 처리 로직 (기존 계정 연동 포함)
     */
    @Transactional
    public Long kakaoLogin(String code) {
        // 1. "인가 코드"로 "Access Token" 요청
        String accessToken = getKakaoAccessToken(code);

        // 2. "Access Token"으로 카카오 사용자 정보 조회
        Map<String, Object> kakaoUserInfo = getKakaoUserInfo(accessToken);

        String kakaoId = (String) kakaoUserInfo.get("id");
        String email = (String) kakaoUserInfo.get("email");
        String nickname = kakaoUserInfo.get("nickname") != null ? (String) kakaoUserInfo.get("nickname") : "갓생러";

        // 3. 우리 DB에 이미 가입된 이메일인지 확인
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .name(nickname)
                    .nickname(nickname)
                    .provider("KAKAO")
                    .providerId(kakaoId)
                    .build();
            return userRepository.save(newUser);
        });

        // 4. 유저 PK 반환
        return user.getId();
    }

    // ==========================================
    // 🛠 카카오 API 통신용 헬퍼 메서드
    // ==========================================

    private String getKakaoAccessToken(String code) {

        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);
        params.add("client_secret", kakaoClientSecret);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        return (String) responseBody.get("access_token");
    }

    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<Void> kakaoProfileRequest = new HttpEntity<>(headers);

        ResponseEntity<Map> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        Long kakaoId = (Long) body.get("id");

        // 카카오 응답 구조(kakao_account 및 profile)에서 이메일과 닉네임 파싱
        Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
        String email = (String) kakaoAccount.get("email");

        Map<String, Object> properties = (Map<String, Object>) body.get("properties");
        String nickname = properties != null ? (String) properties.get("nickname") : null;

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", String.valueOf(kakaoId));
        userInfo.put("email", email);
        userInfo.put("nickname", nickname);

        return userInfo;
    }

    public String getKakaoLoginUrl() {
        return "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri
                + "&response_type=code";
    }
}