package com.primeshop.category;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private List<CategorySpecResponse> specs;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.slug = category.getSlug();
        this.specs = category.getSpecs().stream()
            .map(CategorySpecResponse::new)
            .collect(Collectors.toList());
    }
}
