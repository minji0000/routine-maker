package com.kari.routinemaker.service;

import com.kari.routinemaker.domain.Category;
import com.kari.routinemaker.domain.User;
import com.kari.routinemaker.repository.CategoryRepository;
import com.kari.routinemaker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // 유저의 카테고리 목록 조회
    public List<Category> getCategories(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return categoryRepository.findByUser(user);
    }

    // 카테고리 생성
    @Transactional
    public Category createCategory(Long userId, String name, String color) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Category category = Category.builder()
                .user(user)
                .name(name)
                .color(color != null ? color : "purple")
                .build();

        return categoryRepository.save(category);
    }
}