package com.serviceconnect.controller;

import com.serviceconnect.dto.ApiResponse;
import com.serviceconnect.model.ServiceCategory;
import com.serviceconnect.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceCategory>>> getAllCategories() {
        List<ServiceCategory> categories = categoryService.getAllActive();
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceCategory>> getCategoryById(@PathVariable Long id) {
        ServiceCategory category = categoryService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(category));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceCategory>> createCategory(@RequestBody ServiceCategory category) {
        ServiceCategory created = categoryService.createCategory(category);
        return ResponseEntity.ok(ApiResponse.ok("Category created successfully", created));
    }
}
