package com.primeshop.news;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class NewsServiceImpl implements NewsService {

    private static final String NEWS_NOT_FOUND = "News not found";
    private static final String CATEGORY_NOT_FOUND = "Category not found";

    private final NewsRepository newsRepository;
    private final NewsCategoryRepo newsCategoryRepository;

    @Autowired
    public NewsServiceImpl(NewsRepository newsRepository, NewsCategoryRepo newsCategoryRepository) {
        this.newsRepository = newsRepository;
        this.newsCategoryRepository = newsCategoryRepository;
    }

    @Override
    public Page<NewsResponse> getAllNews(Pageable pageable, Long categoryId, String search) {
        if (categoryId != null && !newsCategoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException(CATEGORY_NOT_FOUND);
        }
        if (categoryId != null || StringUtils.hasText(search)) {
            return newsRepository.searchNewsByCategory(categoryId, search, pageable)
                    .map(this::convertToResponse);
        }
        return newsRepository.findByStatus(NewsStatus.PUBLISHED, pageable)
                .map(this::convertToResponse);
    }

    @Override
    public NewsResponse getNewsById(Long id) {
        return newsRepository.findById(id)
                .map(this::convertToResponse)
                .orElseThrow(() -> new IllegalArgumentException(NEWS_NOT_FOUND));
    }

    @Override
    @Transactional
    public NewsResponse getNewsBySlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("Slug cannot be empty");
        }
        News news = newsRepository.findBySlugAndStatus(slug, NewsStatus.PUBLISHED)
                .orElseThrow(() -> new IllegalArgumentException(NEWS_NOT_FOUND));
        newsRepository.incrementViewCount(news.getId());
        return convertToResponse(news);
    }

    @Override
    @Transactional
    public NewsResponse createNews(NewsRequest newsRequest) {
        validateNewsRequest(newsRequest);
        NewsCategory category = validateAndGetCategory(newsRequest.getCategoryId());
        News news = new News();
        news.setTitle(newsRequest.getTitle());
        news.setSlug(generateSlug(newsRequest.getTitle()));
        news.setTextUrl(newsRequest.getTextUrl());
        news.setExcerpt(newsRequest.getExcerpt());
        news.setImageUrl(newsRequest.getImageUrl());
        news.setCategory(category);
        news.setAuthor(null);
        news.setStatus(NewsStatus.valueOf(newsRequest.getStatus()));
        if (news.getStatus() == NewsStatus.PUBLISHED && news.getPublishedAt() == null) {
            news.setPublishedAt(LocalDateTime.now());
        }
        news.setViewCount(0);
        News savedNews = newsRepository.save(news);
        return convertToResponse(savedNews);
    }

    @Override
    @Transactional
    public NewsResponse updateNews(Long id, NewsRequest newsRequest) {
        validateNewsRequest(newsRequest);
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(NEWS_NOT_FOUND));
        NewsCategory category = validateAndGetCategory(newsRequest.getCategoryId());
        news.setTitle(newsRequest.getTitle());
        news.setSlug(generateSlug(newsRequest.getTitle()));
        news.setTextUrl(newsRequest.getTextUrl());
        news.setExcerpt(newsRequest.getExcerpt());
        news.setImageUrl(newsRequest.getImageUrl());
        news.setCategory(category);
        news.setAuthor(null);
        news.setStatus(NewsStatus.valueOf(newsRequest.getStatus()));
        if (news.getStatus() == NewsStatus.PUBLISHED && news.getPublishedAt() == null) {
            news.setPublishedAt(LocalDateTime.now());
        }
        News updatedNews = newsRepository.save(news);
        return convertToResponse(updatedNews);
    }

    @Override
    @Transactional
    public boolean deleteNews(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(NEWS_NOT_FOUND));
        newsRepository.delete(news);
        return true;
    }

    private NewsResponse convertToResponse(News news) {
        NewsResponse response = new NewsResponse();
        response.setId(news.getId());
        response.setTitle(news.getTitle());
        response.setSlug(news.getSlug());
        response.setExcerpt(news.getExcerpt());
        response.setImageUrl(news.getImageUrl());
        response.setTextUrl(news.getTextUrl());
        response.setCategoryId(news.getCategory() != null ? news.getCategory().getId() : null); // Sửa thành getUserId()
        response.setStatus(news.getStatus().name());
        response.setPublishedAt(news.getPublishedAt());
        response.setCreatedAt(news.getCreatedAt());
        response.setUpdatedAt(news.getUpdatedAt());
        response.setViewCount(news.getViewCount());
        return response;
    }

    private String generateSlug(String title) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Title cannot be empty for slug generation");
        }
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }

    private void validateNewsRequest(NewsRequest newsRequest) {
        if (!StringUtils.hasText(newsRequest.getTitle())) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (!StringUtils.hasText(newsRequest.getTextUrl())) {
            throw new IllegalArgumentException("textUrl cannot be empty");
        }
        if (!StringUtils.hasText(newsRequest.getStatus())) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        try {
            NewsStatus.valueOf(newsRequest.getStatus());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + newsRequest.getStatus());
        }
    }

    private NewsCategory validateAndGetCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return newsCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException(CATEGORY_NOT_FOUND));
    }

    @Override
    public long countNews() {
        return newsRepository.count();
    }
}