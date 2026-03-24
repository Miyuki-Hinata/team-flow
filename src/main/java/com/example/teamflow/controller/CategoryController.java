package com.example.teamflow.controller;

import com.example.teamflow.entity.Category;
import com.example.teamflow.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/api/categories")
    public List<Category> getCategories() {
        return categoryService.getCategories();
    }

    @GetMapping("/api/categories/{id}")
    public Category getCategoryById(
            @PathVariable Long id
    ) {
        return categoryService.getCategoryById(id);
    }

    @PostMapping("/api/categories")
    public Category createCategory(
            @Valid @RequestBody Category category
    ) {
        return categoryService.createCategory(category);
    }

    @PutMapping("/api/categories/{id}")
    public Category updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody Category category
    ) {
        return categoryService.updateCategory(id, category);
    }

    @DeleteMapping("/api/categories/{id}")
    public String deleteCategory(
            @PathVariable Long id
    ) {
        return categoryService.deleteCategory(id);
    }
}



//GET    /api/categories          → 一般ユーザーOK
//GET    /api/categories/{id}     → 一般ユーザーOK
//POST   /api/categories          → 管理者のみ
//PUT    /api/categories/{id}     → 管理者のみ
//DELETE /api/categories/{id}     → 管理者のみ