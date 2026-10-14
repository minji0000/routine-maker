package com.kari.routinemaker.repository;

import com.kari.routinemaker.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 회원 정보를 조회하는 쿼리 메서드 (로그인 및 중복 가입 체크용)
    Optional<User> findByEmail(String email);

    // 이메일 존재 여부를 boolean 값으로 빠르게 확인하는 메서드 (회원가입 시 중복 검사)
    boolean existsByEmail(String email);
}