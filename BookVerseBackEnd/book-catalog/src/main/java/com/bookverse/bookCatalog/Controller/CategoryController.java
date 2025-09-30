package com.bookverse.bookCatalog.Controller;

import com.bookverse.bookCatalog.Models.Category;
import com.bookverse.bookCatalog.Service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Category management operations")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Get all categories", description = "Retrieves a list of all categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved categories",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Category.class)))
    })
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

 
    // Fetches a single category by its ID.
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryByIdOrThrow(id);
        return ResponseEntity.ok(category);
    }
    
   
    @Operation(summary = "Create new category", description = "Creates a new category in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Category created successfully",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<Category> createCategory(
            @Parameter(description = "Category to create") @RequestBody Category category) {
        return new ResponseEntity<>(categoryService.saveCategory(category), HttpStatus.CREATED);
    }
    

    // Updates an existing category.
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category updatedCategory = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(updatedCategory);
    }

    // Deletes a category from the database.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    // Get active categories for navigation (sorted by display order)
    @Operation(summary = "Get active categories for navigation", description = "Retrieves active categories sorted by display order for navigation menu")
    @GetMapping("/active")
    public List<Category> getActiveCategoriesForNavigation() {
        return categoryService.getActiveCategoriesForNavigation();
    }
    
    // Toggle category active status
    @Operation(summary = "Toggle category active status", description = "Toggles the active status of a category")
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Category> toggleCategoryActiveStatus(@PathVariable Long id) {
        Category updatedCategory = categoryService.toggleCategoryActiveStatus(id);
        return ResponseEntity.ok(updatedCategory);
    }
}