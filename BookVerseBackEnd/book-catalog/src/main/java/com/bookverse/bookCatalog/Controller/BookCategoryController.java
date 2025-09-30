package com.bookverse.bookCatalog.Controller;

import com.bookverse.bookCatalog.Models.BookCategory;
import com.bookverse.bookCatalog.Service.BookCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Remove @CrossOrigin - CORS handled by API Gateway
@RestController
@RequestMapping("/api/book-categories")

public class BookCategoryController {

    private final BookCategoryService bookCategoryService;

    public BookCategoryController(BookCategoryService bookCategoryService) {
        this.bookCategoryService = bookCategoryService;
    }

    //Links a book to a category.
    @PostMapping
    public ResponseEntity<BookCategory> linkBookToCategory(@RequestParam Long bookId, @RequestParam Long categoryId, @RequestParam(defaultValue = "0") int priority) {
        return bookCategoryService.linkBookToCategory(bookId, categoryId, priority)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }


    //Unlinks a book from a category.
    @DeleteMapping
    public ResponseEntity<Void> unlinkBookFromCategory(@RequestParam Long bookId, @RequestParam Long categoryId) {
        bookCategoryService.unlinkBookFromCategory(bookId, categoryId);
        return ResponseEntity.noContent().build();
    }
}