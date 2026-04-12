package com.example.teamflow.service;

import com.example.teamflow.entity.Category;
import com.example.teamflow.exception.ResourceNotFoundException;
import com.example.teamflow.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するカテゴリーがありません id: " + id));
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category category) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するカテゴリーがありません id: " + id));

        existingCategory.setCategoryName(category.getCategoryName());
        return categoryRepository.save(existingCategory);
    }

    public String deleteCategory(Long id) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("該当するカテゴリーがありません id: " + id));

        existingCategory.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(existingCategory);

        return "category_id = " + id + "を削除しました";
    }
}
