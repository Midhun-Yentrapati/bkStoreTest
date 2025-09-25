package com.bookverse.bookCatalog.Controller;

import com.bookverse.bookCatalog.Models.Books;
import com.bookverse.bookCatalog.Models.Category;
import com.bookverse.bookCatalog.Service.BookService;
import com.bookverse.bookCatalog.Service.CategoryService;
import com.bookverse.bookCatalog.DTO.BookCreateRequest;
import com.bookverse.bookCatalog.DTO.BookWithRelations;
import com.bookverse.bookCatalog.DTO.BookImageRequest;

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
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Book management operations")
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;

    public BookController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @Operation(summary = "Get all active books", description = "Retrieves a list of all active books with their relationships")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved books",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = BookWithRelations.class)))
    })
    @GetMapping
    public List<BookWithRelations> getAllBooks() {
        return bookService.getAllBooksWithRelations();
    }
    
    // Fetches all books for admin (including inactive ones)
    @GetMapping("/admin/all")
    public List<Books> getAllBooksForAdmin() {
        return bookService.getAllBooksForAdmin();
    }

    @Operation(summary = "Get book by ID", description = "Retrieves a specific book by its ID with all relationships")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book found",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = BookWithRelations.class))),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookWithRelations> getBookById(
            @Parameter(description = "ID of the book to retrieve") @PathVariable Long id) {
        return bookService.getBookByIdWithRelations(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Create a new book", description = "Creates a new book in the catalog")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Book created successfully",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Books.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
	@PostMapping(consumes = "application/json",produces = "application/json")
    public ResponseEntity<Books> createBook(
            @Parameter(description = "Book creation request") @RequestBody BookCreateRequest bookRequest) {
		Books newBook = bookService.createBookFromRequest(bookRequest);
        return new ResponseEntity<>(newBook, HttpStatus.CREATED);
    }
    
	// To update a book with id
    @PutMapping("/{id}")
    public ResponseEntity<Books> updateBook(@PathVariable Long id, @RequestBody BookCreateRequest bookRequest) {
        Books updatedBook = bookService.updateBookFromRequest(id, bookRequest);
        return ResponseEntity.ok(updatedBook);
    }

    // To soft delete a book with id (sets isActive to false)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
    
    // To restore a soft-deleted book (sets isActive to true)
    @PutMapping("/{id}/restore")
    public ResponseEntity<String> restoreBook(@PathVariable Long id) {
        bookService.restoreBook(id);
        return ResponseEntity.ok("Book restored successfully");
    }

    // To modify stock count in Books table
    @PutMapping("/{id}/stock")
    public ResponseEntity<String> decreaseStock(@PathVariable Long id, @RequestParam int quantity) {
        bookService.decreaseStock(id, quantity);
        return ResponseEntity.ok("Stock decreased successfully by " + quantity);
    }

    @Operation(summary = "Search books", description = "Search books by title, author, or other criteria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Books.class)))
    })
    @GetMapping("/search")
    public List<Books> searchBooks(
            @Parameter(description = "Search query for book title or author") @RequestParam String query) {
        return bookService.searchBooks(query);
    }

    // Returns similar books to a book based on it's categories
    @GetMapping("/{id}/similar")
    public List<BookWithRelations> getSimilarBooks(@PathVariable Long id) {
        return bookService.getSimilarBooksWithRelations(id);
    }

    // Fetches books related to sections in Home Page
    @GetMapping("/sales-category/{salesCategory}")
    public List<BookWithRelations> getBooksBySalesCategory(@PathVariable Books.SalesCategory salesCategory) {
        return bookService.getBooksBySalesCategoryWithRelations(salesCategory);
    }

    // Used for analytics
    @GetMapping("/highly-sold")
    public List<Books> getHighlySoldBooks(@RequestParam(defaultValue = "10") int limit) {
        return bookService.getBooksBySalesCategoryAndSort(Books.SalesCategory.BEST_SELLING, limit, "desc");
    }

    // Used for analytics
    @GetMapping("/least-sold")
    public List<Books> getLeastSoldBooks(@RequestParam(defaultValue = "10") int limit) {
        return bookService.getBooksBySalesCategoryAndSort(Books.SalesCategory.BEST_SELLING, limit, "asc");
    }

    @PutMapping("/category/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category updatedCategory = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(updatedCategory);
    }
    
    // Update book categories
    @Operation(summary = "Update book categories", description = "Updates the categories associated with a book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book categories updated successfully",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Books.class))),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "400", description = "Invalid category IDs")
    })
    @PutMapping("/{id}/categories")
    public ResponseEntity<Books> updateBookCategories(
            @Parameter(description = "ID of the book to update") @PathVariable Long id,
            @Parameter(description = "List of category IDs") @RequestBody List<Long> categoryIds) {
        Books updatedBook = bookService.updateBookCategories(id, categoryIds);
        return ResponseEntity.ok(updatedBook);
    }
    
    // Update book images
    @Operation(summary = "Update book images", description = "Updates the images associated with a book")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book images updated successfully",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Books.class))),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "400", description = "Invalid image data")
    })
    @PutMapping("/{id}/images")
    public ResponseEntity<Books> updateBookImages(
            @Parameter(description = "ID of the book to update") @PathVariable Long id,
            @Parameter(description = "List of book images") @RequestBody List<BookImageRequest> images) {
        Books updatedBook = bookService.updateBookImages(id, images);
        return ResponseEntity.ok(updatedBook);
    }
}