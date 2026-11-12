package com.kari.routinemaker.controller;

import com.kari.routinemaker.dto.UserRequestDto;
import com.kari.routinemaker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API 엔드포인트
     * POST /api/users/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody UserRequestDto.SignupRequest request) {
        Long savedUserId = userService.signup(request);
        return ResponseEntity.ok(savedUserId);
    }

    /**
     * 로그인 API 엔드포인트
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<Long> login(@RequestBody UserRequestDto.LoginRequest request, HttpSession session) {
        Long userId = userService.login(request);
        session.setAttribute("userId", userId);
        return ResponseEntity.ok(userId);
    }

    /**
     * 로그아웃 API 엔드포인트
     * POST /api/users/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        // 세션이 존재하면 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 카카오 인증 URL을 제공하는 API
     */
    @GetMapping("/kakao/login-url")
    public ResponseEntity<String> getKakaoLoginUrl() {
        String kakaoAuthUrl = userService.getKakaoLoginUrl();
        return ResponseEntity.ok(kakaoAuthUrl);
    }

    /**
     * 2. 카카오 로그인 완료 후 인가 코드(code)를 받아 처리하는 콜백 API
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<Void> kakaoCallback(@RequestParam("code") String code, HttpSession session) {
        // 서비스의 카카오 로그인 로직 호출 (토큰 발급 및 유저 정보 조회·저장)
        Long userId = userService.kakaoLogin(code);

        session.setAttribute("userId", userId);

        // 2. 로그인이 완료되면 메인 페이지(index.html)로 리다이렉트
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/dashboard.html")
                .build();
    }


}