package com.kari.routinemaker.repository;

import com.kari.routinemaker.domain.Category;
import com.kari.routinemaker.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);
}