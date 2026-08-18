package com.primeshop.category;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategorySpecResponse {
    private String name;

    public CategorySpecResponse(CategorySpec spec) {
        this.name = spec.getName();
    }
}
