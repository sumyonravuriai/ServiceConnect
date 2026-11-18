package com.serviceconnect.service;

import com.serviceconnect.dao.CategoryDao;
import com.serviceconnect.exception.ResourceNotFoundException;
import com.serviceconnect.model.ServiceCategory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryDao categoryDao;

    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public List<ServiceCategory> getAllActive() {
        return categoryDao.findAllActive();
    }

    public ServiceCategory getById(Long id) {
        return categoryDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID " + id));
    }

    public ServiceCategory createCategory(ServiceCategory cat) {
        if (cat.getSlug() == null || cat.getSlug().isBlank()) {
            cat.setSlug(cat.getName().toLowerCase().replaceAll("[^a-z0-9]", "-"));
        }
        categoryDao.create(cat);
        return cat;
    }
}
