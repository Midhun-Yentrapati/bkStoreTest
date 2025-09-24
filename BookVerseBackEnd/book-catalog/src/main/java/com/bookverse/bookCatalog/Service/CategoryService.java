package com.bookverse.bookCatalog.Service;

import com.bookverse.bookCatalog.Models.Category;
import com.bookverse.bookCatalog.Repository.CategoryRepository;
import com.bookverse.bookCatalog.Exception.CategoryNotFoundException;
import com.bookverse.bookCatalog.Exception.DuplicateResourceException;
import com.bookverse.bookCatalog.Exception.ValidationException;
import com.bookverse.bookCatalog.Exception.BusinessLogicException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Retrieves all categories.
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Fetches a category by its ID.
    public Optional<Category> getCategoryById(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("Category ID must be a positive number");
        }
        return categoryRepository.findById(id);
    }
    
    // Fetches a category by its ID or throws exception if not found
    public Category getCategoryByIdOrThrow(Long id) {
        return getCategoryById(id)
            .orElseThrow(() -> new CategoryNotFoundException(id));
    }
    
    // Fetches a category by its name.
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    // Saves a new category.
    @Transactional
    public Category saveCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new ValidationException("Category name is required");
        }
        
        // Check for duplicate name
        Optional<Category> existingCategory = categoryRepository.findByName(category.getName().trim());
        if (existingCategory.isPresent()) {
            throw new DuplicateResourceException("Category", "name: " + category.getName());
        }
        
        category.setName(category.getName().trim());
        
        // Auto-generate slug from name
        if (category.getSlug() == null || category.getSlug().trim().isEmpty()) {
            category.setSlug(generateSlug(category.getName()));
        }
        
        // Set default values if not provided
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        if (category.getDisplayOrder() == 0) {
            category.setDisplayOrder(getNextDisplayOrder());
        }
        
        try {
            return categoryRepository.save(category);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to save category: " + e.getMessage(), e);
        }
    }

    // Deletes a category by its ID.
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    // Updates an existing category.
    @Transactional
    public Category updateCategory(Long id, Category updatedCategory) {
        if (id == null || id <= 0) {
            throw new ValidationException("Category ID must be a positive number");
        }
        
        Category existingCategory = getCategoryByIdOrThrow(id);
        
        // Validate updated data
        if (updatedCategory.getName() != null) {
            if (updatedCategory.getName().trim().isEmpty()) {
                throw new ValidationException("Category name cannot be empty");
            }
            
            // Check for duplicate name if name is being updated
            if (!updatedCategory.getName().trim().equals(existingCategory.getName())) {
                Optional<Category> categoryWithSameName = categoryRepository.findByName(updatedCategory.getName().trim());
                if (categoryWithSameName.isPresent()) {
                    throw new DuplicateResourceException("Category", "name: " + updatedCategory.getName());
                }
            }
            existingCategory.setName(updatedCategory.getName().trim());
            // Auto-update slug when name changes
            existingCategory.setSlug(generateSlug(updatedCategory.getName().trim()));
        }
        
        if (updatedCategory.getSlug() != null && !updatedCategory.getSlug().trim().isEmpty()) {
            existingCategory.setSlug(updatedCategory.getSlug().trim());
        }
        if (updatedCategory.getDescription() != null) existingCategory.setDescription(updatedCategory.getDescription());
        if (updatedCategory.getImage() != null) existingCategory.setImage(updatedCategory.getImage());
        if (updatedCategory.getDisplayOrder() >= 0) existingCategory.setDisplayOrder(updatedCategory.getDisplayOrder());
        if (updatedCategory.getIsActive() != null) existingCategory.setIsActive(updatedCategory.getIsActive());
        
        try {
            return categoryRepository.save(existingCategory);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to update category: " + e.getMessage(), e);
        }
    }

    // Searches for categories based on a query.
    public List<Category> searchCategories(String query) {
        return categoryRepository.findByNameContainingIgnoreCase(query);
    }
    
    // Generate a URL-friendly slug from category name
    private String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        
        // Normalize the string to remove accents and diacritics
        String normalized = Normalizer.normalize(name.trim(), Normalizer.Form.NFD);
        
        // Remove all non-ASCII characters
        String ascii = normalized.replaceAll("[^\\p{ASCII}]", "");
        
        // Convert to lowercase and replace spaces and special characters with hyphens
        String slug = ascii.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters except spaces and hyphens
            .replaceAll("\\s+", "-") // Replace spaces with hyphens
            .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
            .replaceAll("^-|-$", ""); // Remove leading and trailing hyphens
        
        return slug;
    }
    
    // Get the next display order for new categories
    private int getNextDisplayOrder() {
        List<Category> allCategories = categoryRepository.findAll();
        if (allCategories.isEmpty()) {
            return 1;
        }
        
        int maxOrder = allCategories.stream()
            .mapToInt(Category::getDisplayOrder)
            .max()
            .orElse(0);
        
        return maxOrder + 1;
    }
    
    // Get categories ordered by display order and active status for navigation
    public List<Category> getActiveCategoriesForNavigation() {
        return categoryRepository.findAll().stream()
            .filter(Category::getIsActive)
            .sorted((c1, c2) -> Integer.compare(c1.getDisplayOrder(), c2.getDisplayOrder()))
            .toList();
    }
    
    // Toggle category active status
    @Transactional
    public Category toggleCategoryActiveStatus(Long id) {
        Category category = getCategoryByIdOrThrow(id);
        category.setIsActive(!category.getIsActive());
        
        try {
            return categoryRepository.save(category);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to toggle category status: " + e.getMessage(), e);
        }
    }
}