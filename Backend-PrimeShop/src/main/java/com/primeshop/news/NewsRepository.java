package com.primeshop.news;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findByStatus(NewsStatus status, Pageable pageable);

    Optional<News> findBySlugAndStatus(String slug, NewsStatus status);

    @Query("SELECT n FROM News n WHERE (:categoryId IS NULL OR n.category.id = :categoryId) " +
           "AND (:search IS NULL OR n.title LIKE %:search%) AND n.status = 'PUBLISHED'")
    Page<News> searchNewsByCategory(Long categoryId, String search, Pageable pageable);

    @Modifying
    @Query("UPDATE News n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    void incrementViewCount(Long id);

    @Query("SELECT COUNT(n) FROM News n")
    Long countByNews();
}