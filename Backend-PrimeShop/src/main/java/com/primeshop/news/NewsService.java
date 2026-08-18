package com.primeshop.news;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsService {
    Page<NewsResponse> getAllNews(Pageable pageable, Long categoryId, String search);

    NewsResponse getNewsById(Long id);

    NewsResponse getNewsBySlug(String slug);

    NewsResponse createNews(NewsRequest newsRequest);

    NewsResponse updateNews(Long id, NewsRequest newsRequest);

    boolean deleteNews(Long id);

    long countNews();
}