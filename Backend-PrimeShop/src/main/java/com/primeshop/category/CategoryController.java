package com.primeshop.category;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CategoryController {
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private CategoryService categoryService;

    // @GetMapping
    // public ResponseEntity<List<CategoryResponse>> getAllCategories() {
    //     return ResponseEntity.ok(categoryService.getAllCategories());
    // }

    @GetMapping
    public ResponseEntity<?> getCategories() {
        List<String> categories = categoryRepo.findDistinctNames();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/slugs")
    public ResponseEntity<?> getCategorySlugs() {
        List<String> slugs = categoryRepo.findDistinctSlugs();
        return ResponseEntity.ok(slugs);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
    
    @GetMapping("/count")
    public ResponseEntity<?> getCategoriesCount() {
        long count = categoryRepo.count();
        return ResponseEntity.ok(count);
    }
}
