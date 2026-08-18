package com.primeshop.category;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    @Autowired CategoryRepo categoryRepo;

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepo.findAll();
        return categories.stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getCategories() {
        List<Category> categories = categoryRepo.findAll();
        return categories.stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }

    public List<String> getCategorySlugs() {
        return categoryRepo.findDistinctSlugs();
    }
    
    public long countCategory() {
        return categoryRepo.count();
    }
}
