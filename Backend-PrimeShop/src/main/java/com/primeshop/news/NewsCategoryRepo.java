package com.primeshop.news;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NewsCategoryRepo extends JpaRepository<NewsCategory, Long> {
    List<NewsCategory> findByType(NewsCategoryType type);
}
