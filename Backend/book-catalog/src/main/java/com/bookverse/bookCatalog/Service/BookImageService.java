package com.bookverse.bookCatalog.Service;

import com.bookverse.bookCatalog.Models.BookImage;
import com.bookverse.bookCatalog.Models.Books;
import com.bookverse.bookCatalog.Repository.BookImageRepository;
import com.bookverse.bookCatalog.Repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookImageService {

    private final BookImageRepository bookImageRepository;
    private final BookRepository bookRepository;

    public BookImageService(BookImageRepository bookImageRepository, BookRepository bookRepository) {
        this.bookImageRepository = bookImageRepository;
        this.bookRepository = bookRepository;
    }
    
    // Retrieves all images for a specific book.
    public List<BookImage> getImagesByBookId(Long bookId) {
        return bookImageRepository.findByBookIdOrderByDisplayOrderAsc(bookId);
    }
    
    // Adds a new image to a book.
    @Transactional
    public Optional<BookImage> addImageToBook(Long bookId, BookImage bookImage) {
        Optional<Books> bookOptional = bookRepository.findById(bookId);
        if (bookOptional.isPresent()) {
            Books book = bookOptional.get();
            bookImage.setBook(book);
            
            // If the new image is primary, set all other images for this book to non-primary
            if (bookImage.isPrimary()) {
                bookImageRepository.findByBookIdOrderByDisplayOrderAsc(bookId).forEach(image -> {
                    if (image.isPrimary()) {
                        image.setPrimary(false);
                        bookImageRepository.save(image);
                    }
                });
            }
            return Optional.of(bookImageRepository.save(bookImage));
        }
        return Optional.empty();
    }
    
    // Deletes a specific image by its ID.
    public void deleteImage(Long imageId) {
        bookImageRepository.deleteById(imageId);
    }
}