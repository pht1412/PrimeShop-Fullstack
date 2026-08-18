package com.primeshop.category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategorySpecRepo extends JpaRepository<CategorySpec, Long> {
    CategorySpec findById(Integer id);
}
