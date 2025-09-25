package com.bookverse.bookCatalog.Service;

import com.bookverse.bookCatalog.Models.BookCategory;
import com.bookverse.bookCatalog.Models.Books;
import com.bookverse.bookCatalog.Models.Category;
import com.bookverse.bookCatalog.Repository.BookCategoryRepository;
import com.bookverse.bookCatalog.Repository.BookRepository;
import com.bookverse.bookCatalog.Repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BookCategoryService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookCategoryRepository bookCategoryRepository;

    public BookCategoryService(BookRepository bookRepository, CategoryRepository categoryRepository, BookCategoryRepository bookCategoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.bookCategoryRepository = bookCategoryRepository;
    }
    
    /**
     * Links a book to a category.
     * This method is an explicit representation of the relationship table logic.
     * @param bookId The ID of the book.
     * @param categoryId The ID of the category.
     * @param priority The priority of this category link.
     * @return An Optional of the saved BookCategory.
     */
    public Optional<BookCategory> linkBookToCategory(Long bookId, Long categoryId, int priority) {
        Books book = bookRepository.findById(bookId).orElse(null);
        Category category = categoryRepository.findById(categoryId).orElse(null);

        if (book != null && category != null) {
            BookCategory bookCategory = new BookCategory();
            bookCategory.setBook(book);
            bookCategory.setCategory(category);
            bookCategory.setPriority(priority);
            return Optional.of(bookCategoryRepository.save(bookCategory));
        }
        return Optional.empty();
    }
    
    /**
     * Unlinks a book from a category using the book and category IDs.
     * @param bookId The ID of the book.
     * @param categoryId The ID of the category.
     */
    @Transactional
    public void unlinkBookFromCategory(Long bookId, Long categoryId) {
        bookCategoryRepository.deleteByBookIdAndCategoryId(bookId, categoryId);
    }
}