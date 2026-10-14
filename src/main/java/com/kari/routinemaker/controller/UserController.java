package com.kari.routinemaker.controller;

import com.kari.routinemaker.dto.UserRequestDto;
import com.kari.routinemaker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Long> login(@RequestBody UserRequestDto.LoginRequest request) {
        Long userId = userService.login(request);
        return ResponseEntity.ok(userId);
    }
}