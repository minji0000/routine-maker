package com.kari.routinemaker.controller;

import com.kari.routinemaker.domain.Category;
import com.kari.routinemaker.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 목록 조회 API
    @GetMapping
    public ResponseEntity<List<Category>> getCategories(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build(); // 인증되지 않음
        }

        List<Category> categories = categoryService.getCategories(userId);
        return ResponseEntity.ok(categories);
    }

    // 카테고리 생성 API
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Map<String, String> request, HttpSession session) {

        System.out.println("=== 세션 확인용 userId: " + session.getAttribute("userId"));

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        String name = request.get("name");
        String color = request.get("color");

        Category savedCategory = categoryService.createCategory(userId, name, color);
        return ResponseEntity.ok(savedCategory);
    }
}