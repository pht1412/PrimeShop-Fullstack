package com.primeshop.news;

import jakarta.validation.constraints.NotBlank;

public class NewsRequest {
    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "textUrl cannot be empty")
    private String textUrl; // Thêm textUrl

    private String excerpt;

    private String imageUrl;

    private Long categoryId;

    @NotBlank(message = "Status cannot be empty")
    private String status;

    // Getters
    public String getTitle() {
        return title;
    }

    public String getTextUrl() {
        return textUrl;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setTextUrl(String textUrl) {
        this.textUrl = textUrl;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}