package com.kari.routinemaker.service;

import com.kari.routinemaker.domain.User;
import com.kari.routinemaker.dto.UserRequestDto;
import com.kari.routinemaker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화 인코더 주입

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
                .password(encodedPassword) // 평문 대신 암호화된 패스워드 저장!
                .name(request.getName())
                .nickname(request.getNickname())
                .provider("LOCAL")
                .build();

        // 4. DB에 저장 후 생성된 PK(id) 반환
        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }
}