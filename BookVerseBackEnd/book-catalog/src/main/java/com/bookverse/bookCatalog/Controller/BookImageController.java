package com.bookverse.bookCatalog.Controller;

import com.bookverse.bookCatalog.Models.BookImage;
import com.bookverse.bookCatalog.Service.BookImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/books/{bookId}/images")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")

public class BookImageController {

    private final BookImageService bookImageService;

    public BookImageController(BookImageService bookImageService) {
        this.bookImageService = bookImageService;
    }

    // Retrieves all images for a specific book.
    @GetMapping
    public List<BookImage> getImagesForBook(@PathVariable Long bookId) {
        return bookImageService.getImagesByBookId(bookId);
    }
    
    // Adds a new image to a specific book.
    @PostMapping
    public ResponseEntity<BookImage> addImageToBook(@PathVariable Long bookId, @RequestBody BookImage bookImage) {
        return bookImageService.addImageToBook(bookId, bookImage)
                .map(image -> new ResponseEntity<>(image, HttpStatus.CREATED))
                .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    // Deletes a specific image.
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long bookId, @PathVariable Long imageId) {
        bookImageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }
}