package com.kari.routinemaker.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //외부에서 무분별하게 객체를 생성하는 것을 막기 위해 기본 생성자의 접근 제어자를 protected(같은 패키지)로 제한
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password; // 소셜 로그인 고려하여 NULL 허용

    @Column(nullable = false, length = 30)
    private String name; // 실명

    @Column(nullable = false, length = 30)
    private String nickname; // 닉네임

    @Column(length = 20)
    private String provider; // LOCAL, KAKAO, NAVER 등

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    //처음 DB에 INSERT 되기 직전에 자동으로 실행되는 메서드
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.provider == null) {
            this.provider = "LOCAL";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public User(String email, String password, String name, String nickname, String provider, String providerId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
    }
}