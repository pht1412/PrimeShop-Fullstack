package com.primeshop.category;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepo extends JpaRepository<Category, Long> {
    <Optional> Category findByName(String name);
    <Optional> Category findBySlug(String slug);

    @Query(value = "SELECT DISTINCT c.name from Category c")
    List<String> findDistinctNames();

    @Query(value = "SELECT DISTINCT c.slug from Category c")
    List<String> findDistinctSlugs();

    @Query("SELECT COUNT(c) FROM Category c")
    Long countByCategory();
}
