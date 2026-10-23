package com.kari.routinemaker.repository;

import com.kari.routinemaker.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}