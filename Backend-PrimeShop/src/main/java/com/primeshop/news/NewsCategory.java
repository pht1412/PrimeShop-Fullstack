package com.primeshop.news;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "categories")
public class NewsCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NewsCategoryType type;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<News> news;

    public NewsCategory() {
    }

    public NewsCategory(Long id, String name, String slug, NewsCategoryType type, List<News> news) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.type = type;
        this.news = news;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public NewsCategoryType getType() {
        return type;
    }

    public List<News> getNews() {
        return news;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setType(NewsCategoryType type) {
        this.type = type;
    }

    public void setNews(List<News> news) {
        this.news = news;
    }
}