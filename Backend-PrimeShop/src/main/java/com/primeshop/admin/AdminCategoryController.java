package com.primeshop.admin;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.primeshop.category.Category;
import com.primeshop.category.CategoryRepo;
import com.primeshop.category.CategoryRequest;
import com.primeshop.category.CategoryService;
import com.primeshop.category.CategorySpec;
import com.primeshop.category.CategorySpecRepo;
import com.primeshop.utils.SlugUtils;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/category")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryRepo categoryRepo;
    private final CategorySpecRepo categorySpecRepo;

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/add")
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(SlugUtils.toSlug(request.getName()));

        Category saved = categoryRepo.save(category);

        List<CategorySpec> attributes = request.getSpecs().stream()
            .map(attr -> new CategorySpec(null, attr, saved))
            .collect(Collectors.toList());

        categorySpecRepo.saveAll(attributes);
        System.out.println("Category API was hit");
        return ResponseEntity.ok("Category created successfully");
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateCategory(@RequestBody CategoryRequest request) {
        Category category = categoryRepo.findBySlug(request.getSlug());
        if (category == null) {
            return ResponseEntity.badRequest().body("Category not found");
        }
        category.setName(request.getName());
        category.setSlug(SlugUtils.toSlug(request.getName()));
        categoryRepo.save(category);
        return ResponseEntity.ok("Category updated successfully");
    }
}
