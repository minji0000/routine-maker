package com.kari.routinemaker.dto;

import lombok.Getter;
import lombok.Setter;

public class UserRequestDto {

    // 회원가입 요청 시 클라이언트에서 전달받을 데이터
    @Getter
    @Setter
    public static class SignupRequest {
        private String email;
        private String password;
        private String name;     // 실명
        private String nickname; // 닉네임
    }

    // 로그인 요청 시 클라이언트에서 전달받을 데이터
    @Getter
    @Setter
    public static class LoginRequest {
        private String email;
        private String password;
    }
}