package com.bookverse.bookCatalog.Service;

import com.bookverse.bookCatalog.Models.Books;
import com.bookverse.bookCatalog.Models.Category;
import com.bookverse.bookCatalog.DTO.BookCreateRequest;
import com.bookverse.bookCatalog.DTO.BookWithRelations;
import com.bookverse.bookCatalog.DTO.BookImageRequest;
import com.bookverse.bookCatalog.Models.BookCategory;
import com.bookverse.bookCatalog.Models.BookImage;
import com.bookverse.bookCatalog.Repository.BookRepository;
import com.bookverse.bookCatalog.Repository.CategoryRepository;
import com.bookverse.bookCatalog.Repository.BookCategoryRepository;
import com.bookverse.bookCatalog.Repository.InventoryAlertRepository;
import com.bookverse.bookCatalog.Exception.BookNotFoundException;
import com.bookverse.bookCatalog.Exception.CategoryNotFoundException;
import com.bookverse.bookCatalog.Exception.InsufficientStockException;
import com.bookverse.bookCatalog.Exception.DuplicateResourceException;
import com.bookverse.bookCatalog.Exception.ValidationException;
import com.bookverse.bookCatalog.Exception.BusinessLogicException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Comparator;    
import java.util.stream.Collectors;


@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookCategoryRepository bookCategoryRepository;
    //private final InventoryAlertRepository inventoryAlertRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository, BookCategoryRepository bookCategoryRepository, InventoryAlertRepository inventoryAlertRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.bookCategoryRepository = bookCategoryRepository;
        //this.inventoryAlertRepository = inventoryAlertRepository;
    }

    // Fetches all active books
    public List<Books> getAllBooks() {
        return bookRepository.findAllWithCategories();
    }
    
    // Fetches all books for admin (including inactive ones)
    public List<Books> getAllBooksForAdmin() {
        return bookRepository.findAllWithCategoriesForAdmin();
    }

    // fetches a book by it's Id
    public Optional<Books> getBookById(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("Book ID must be a positive number");
        }
        return bookRepository.findByIdWithCategories(id);
    }
    
    // fetches a book by it's Id or throws exception if not found
    public Books getBookByIdOrThrow(Long id) {
        return getBookById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
    }
    
    // Creates or saves a new book.
    @Transactional
    public Books createBookFromRequest(BookCreateRequest request) {
        // Validate basic required fields
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ValidationException("Book title is required");
        }
        if (request.getAuthor() == null || request.getAuthor().trim().isEmpty()) {
            throw new ValidationException("Book author is required");
        }
        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new ValidationException("Book price must be a non-negative number");
        }
        
        // Check for duplicate ISBN if provided
        if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty()) {
            Optional<Books> existingBook = bookRepository.findByIsbn(request.getIsbn());
            if (existingBook.isPresent()) {
                throw new DuplicateResourceException("Book", "ISBN: " + request.getIsbn());
            }
        }
        
        Books book = new Books();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle().trim());
        book.setAuthor(request.getAuthor().trim());
        book.setDescription(request.getDescription());
        book.setLanguage(request.getLanguage());
        book.setFormat(request.getFormat());
        book.setEdition(request.getEdition());
        book.setPublisher(request.getPublisher());
        book.setPublicationDate(request.getPublicationDate());
        
        // Numeric fields with validation
        book.setPages(request.getPages() != null ? request.getPages() : 0);
        book.setWeight(request.getWeight() != null ? request.getWeight() : 0.0);
        book.setDimensions(request.getDimensions());
        book.setPrice(request.getPrice());
        book.setMrp(request.getMrp() != null ? request.getMrp() : request.getPrice());
        book.setStockDisplay(request.getStockDisplay() != null ? request.getStockDisplay() : 0);
        book.setStockActual(request.getStockActual() != null ? request.getStockActual() : 0);
        
        // Validate stock values
        if (book.getStockActual() < 0) {
            throw new ValidationException("Actual stock cannot be negative");
        }
        if (book.getStockDisplay() < 0) {
            throw new ValidationException("Display stock cannot be negative");
        }
        
        // Sales category - set default if null
        book.setSalesCategory(request.getSalesCategory() != null ? request.getSalesCategory() : Books.SalesCategory.NEWLY_LAUNCHED);
        
        // Boolean fields with defaults
        book.setActive(request.getIsActive() != null ? request.getIsActive() : true);
        book.setFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);
        
        // Categories - validate they exist
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new CategoryNotFoundException("One or more categories not found");
            }
            Set<BookCategory> bookCategories = categories.stream()
                    .map(category -> {
                        BookCategory bc = new BookCategory();
                        bc.setBook(book);
                        bc.setCategory(category);
                        return bc;
                    })
                    .collect(Collectors.toSet());
            book.setBookCategories(bookCategories);
        }
        
        // Images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            Set<BookImage> bookImages = request.getImages().stream()
                    .map(imageRequest -> {
                        BookImage img = new BookImage();
                        img.setImage(imageRequest.getImageUrl()); // Map imageUrl to image field
                        img.setPrimary(imageRequest.getIsPrimary() != null ? imageRequest.getIsPrimary() : false);
                        img.setAltText(imageRequest.getAltText());
                        img.setDisplayOrder(imageRequest.getDisplayOrder() != null ? imageRequest.getDisplayOrder() : 0);
                        img.setBook(book); // Set the relationship
                        return img;
                    })
                    .collect(Collectors.toSet());
            book.setBookImages(bookImages);
        }
        
        try {
            return bookRepository.save(book);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to create book: " + e.getMessage(), e);
        }
    }

    
    // Updates a book from a BookCreateRequest DTO
    @Transactional
    public Books updateBookFromRequest(Long id, BookCreateRequest request) {
        if (id == null || id <= 0) {
            throw new ValidationException("Book ID must be a positive number");
        }
        
        Books existingBook = getBookByIdOrThrow(id);
        
        // Validate request data
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ValidationException("Book title is required");
        }
        if (request.getAuthor() == null || request.getAuthor().trim().isEmpty()) {
            throw new ValidationException("Book author is required");
        }
        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new ValidationException("Book price must be a non-negative number");
        }
        // Check for ISBN conflicts if ISBN is being updated
        if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty() && 
            !request.getIsbn().equals(existingBook.getIsbn())) {
            Optional<Books> bookWithSameIsbn = bookRepository.findByIsbn(request.getIsbn());
            if (bookWithSameIsbn.isPresent()) {
                throw new DuplicateResourceException("Book", "ISBN: " + request.getIsbn());
            }
        }
        
        // Update basic fields
        if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty()) {
            existingBook.setIsbn(request.getIsbn().trim());
        }
        existingBook.setTitle(request.getTitle().trim());
        existingBook.setAuthor(request.getAuthor().trim());
        existingBook.setDescription(request.getDescription());
        existingBook.setLanguage(request.getLanguage());
        existingBook.setFormat(request.getFormat());
        existingBook.setEdition(request.getEdition());
        existingBook.setPublisher(request.getPublisher());
        existingBook.setPublicationDate(request.getPublicationDate());
        
        if (request.getPages() != null && request.getPages() >= 0) {
            existingBook.setPages(request.getPages());
        }
        if (request.getWeight() != null && request.getWeight() >= 0) {
            existingBook.setWeight(request.getWeight());
        }
        if (request.getDimensions() != null) {
            existingBook.setDimensions(request.getDimensions());
        }
        existingBook.setPrice(request.getPrice());
        if (request.getMrp() != null && request.getMrp() >= 0) {
            existingBook.setMrp(request.getMrp());
        } else {
            existingBook.setMrp(request.getPrice());
        }
        if (request.getStockDisplay() != null && request.getStockDisplay() >= 0) {
            existingBook.setStockDisplay(request.getStockDisplay());
        }
        if (request.getStockActual() != null && request.getStockActual() >= 0) {
            existingBook.setStockActual(request.getStockActual());
        }
        
        // Update sales category and flags
        if (request.getSalesCategory() != null) {
            existingBook.setSalesCategory(request.getSalesCategory());
        }
        if (request.getIsActive() != null) {
            existingBook.setActive(request.getIsActive());
        }
        if (request.getIsFeatured() != null) {
            existingBook.setFeatured(request.getIsFeatured());
        }
        
        // Note: For now, we'll skip updating categories and images in the update method
        // to avoid JPA relationship complexities. These can be managed separately.
        // TODO: Implement separate endpoints for category and image management
        
        try {
            return bookRepository.save(existingBook);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to update book from request: " + e.getMessage(), e);
        }
    }

    // Soft deletes a book by setting isActive to false
    @Transactional
    public void deleteBook(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("Book ID must be a positive number");
        }
        
        // Verify book exists before attempting to soft delete
        Books book = getBookByIdOrThrow(id);
        
        // Check if book is already soft deleted
        if (!book.isActive()) {
            throw new BusinessLogicException("Book is already deleted (inactive)");
        }
        
        try {
            // Soft delete by setting isActive to false
            book.setActive(false);
            book.setDeletedAt(java.time.LocalDateTime.now());
            bookRepository.save(book);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to delete book: " + e.getMessage(), e);
        }
    }
    
    // Hard deletes a book by its ID (for admin use only)
    @Transactional
    public void hardDeleteBook(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("Book ID must be a positive number");
        }
        
        // Verify book exists before attempting to delete
        Books book = getBookByIdOrThrow(id);
        
        try {
            bookRepository.deleteById(id);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to hard delete book: " + e.getMessage(), e);
        }
    }
    
    // Restores a soft-deleted book by setting isActive to true
    @Transactional
    public void restoreBook(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("Book ID must be a positive number");
        }
        
        // Find book even if inactive
        Optional<Books> bookOptional = bookRepository.findById(id);
        if (bookOptional.isEmpty()) {
            throw new BookNotFoundException(id);
        }
        
        Books book = bookOptional.get();
        
        // Check if book is already active
        if (book.isActive()) {
            throw new BusinessLogicException("Book is already active");
        }
        
        try {
            // Restore by setting isActive to true and clearing deletedAt
            book.setActive(true);
            book.setDeletedAt(null);
            bookRepository.save(book);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to restore book: " + e.getMessage(), e);
        }
    }
    
    /**
     * Decreases the stock count of a book after an order is placed.
     * This method handles both actual and display stock.
     */
    @Transactional
    public void decreaseStock(Long bookId, int quantity) {
        if (bookId == null || bookId <= 0) {
            throw new ValidationException("Book ID must be a positive number");
        }
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be a positive number");
        }
        
        Books book = getBookByIdOrThrow(bookId);
        
        if (book.getStockActual() < quantity) {
            throw new InsufficientStockException(bookId, quantity, book.getStockActual());
        }
        
        try {
            book.setStockActual(book.getStockActual() - quantity);
            book.setStockDisplay(Math.max(0, book.getStockDisplay() - quantity));
            book.setNoOfBooksSold(book.getNoOfBooksSold() + quantity);
            book.setTotalRevenue(book.getTotalRevenue() + (book.getPrice() * quantity));
            bookRepository.save(book);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to decrease stock: " + e.getMessage(), e);
        }
    }

    // Searches for books by title, author, or description.
    public List<Books> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
    }
    
    // Finds books that share at least one category with a given book.
    @Transactional
    public List<Books> findSimilarBooks(Long bookId) {
        if (bookId == null || bookId <= 0) {
            throw new ValidationException("Book ID must be a positive number");
        }
        
        Books book = getBookByIdOrThrow(bookId);
        
        try {
            List<Long> categoryIds = bookCategoryRepository.findByBookId(bookId)
                    .stream()
                    .map(bc -> bc.getCategory().getId())
                    .collect(Collectors.toList());
            
            if (categoryIds.isEmpty()) {
                return List.of();
            }
            
            // Get similar book IDs first
            List<Long> similarBookIds = bookCategoryRepository.findByCategoryId(categoryIds.get(0))
                    .stream()
                    .map(bc -> bc.getBook().getId())
                    .filter(id -> !id.equals(bookId)) // Exclude the original book
                    .distinct()
                    .collect(Collectors.toList());
            
            // Fetch full book details with relations
            return similarBookIds.stream()
                    .map(id -> bookRepository.findByIdWithCategories(id))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to find similar books: " + e.getMessage(), e);
        }
    }

    // Finds and sorts books by their SalesCategory.
    public List<Books> findBooksBySalesCategory(Books.SalesCategory salesCategory) {
        return bookRepository.findBySalesCategoryWithRelations(salesCategory);
    }
    
    // Finds and sorts books by a given sales category and sort direction.
    public List<Books> getBooksBySalesCategoryAndSort(Books.SalesCategory salesCategory, int limit, String sortDirection) {
        List<Books> books = bookRepository.findBySalesCategoryWithRelations(salesCategory);
        if ("desc".equalsIgnoreCase(sortDirection)) {
            books.sort((a, b) -> Integer.compare(b.getNoOfBooksSold(), a.getNoOfBooksSold()));
        } else {
            books.sort(Comparator.comparingInt(Books::getNoOfBooksSold));
        }
        return books.stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * Converts a Books entity to a BookWithRelations DTO.
     * This is the central piece of the solution.
     */
    private BookWithRelations convertToBookWithRelations(Books book) {
        if (book == null) {
            return null;
        }
        BookWithRelations dto = new BookWithRelations();
        
        // Map all the book's direct properties
        dto.setId(book.getId());
        dto.setIsbn(book.getIsbn());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setDescription(book.getDescription());
        dto.setLanguage(book.getLanguage());
        dto.setFormat(book.getFormat());
        dto.setEdition(book.getEdition());
        dto.setPublisher(book.getPublisher());
        dto.setPublicationDate(book.getPublicationDate());
        dto.setPages(book.getPages());
        dto.setWeight(book.getWeight());
        dto.setDimensions(book.getDimensions());
        dto.setPrice(book.getPrice());
        dto.setMrp(book.getMrp());
        dto.setStockDisplay(book.getStockDisplay());
        dto.setStockActual(book.getStockActual());
        dto.setNoOfBooksSold(book.getNoOfBooksSold());
        dto.setTotalRevenue(book.getTotalRevenue());
        dto.setAverageRating(book.getAverageRating());
        dto.setReviewCount(book.getReviewCount());
        dto.setSalesCategory(book.getSalesCategory());
        dto.setIsActive(book.isActive());
        dto.setIsFeatured(book.isFeatured());
        dto.setLastSoldAt(book.getLastSoldAt());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());

        // Correctly map the nested categories
        if (book.getBookCategories() != null) {
            dto.setCategories(book.getBookCategories().stream().map(bc -> {
                Category category = bc.getCategory();
                BookWithRelations.CategoryInfo categoryInfo = new BookWithRelations.CategoryInfo();
                categoryInfo.setId(category.getId());
                categoryInfo.setName(category.getName());
                categoryInfo.setSlug(category.getSlug());
                categoryInfo.setDescription(category.getDescription());
                categoryInfo.setImage(category.getImage());
                categoryInfo.setIsActive(category.getIsActive());
                categoryInfo.setPriority(bc.getPriority()); // Priority is in BookCategory, not Category
                return categoryInfo;
            }).collect(Collectors.toList()));
        }

        // Correctly map the nested images
        if (book.getBookImages() != null) {
            dto.setImages(book.getBookImages().stream().map(img -> {
                BookWithRelations.BookImageInfo imageInfo = new BookWithRelations.BookImageInfo();
                imageInfo.setId(img.getId());
                imageInfo.setImageUrl(img.getImage()); // Note: BookImage entity uses 'image' field
                imageInfo.setIsPrimary(img.isPrimary());
                imageInfo.setAltText(img.getAltText());
                return imageInfo;
            }).collect(Collectors.toList()));
        }
        
        return dto;
    }

    /**
     * Fetches all books and converts them to DTOs.
     */
    public List<BookWithRelations> getAllBooksWithRelations() {
        return bookRepository.findAllWithCategories().stream()
                .map(this::convertToBookWithRelations)
                .collect(Collectors.toList());
    }

    /**
     * Fetches a single book by ID and converts it to a DTO.
     */
    public Optional<BookWithRelations> getBookByIdWithRelations(Long id) {
        return bookRepository.findByIdWithCategories(id)
                .map(this::convertToBookWithRelations);
    }

    /**
     * Fetches books by sales category and converts them to DTOs with relations.
     */
    public List<BookWithRelations> getBooksBySalesCategoryWithRelations(Books.SalesCategory salesCategory) {
        return bookRepository.findBySalesCategoryWithRelations(salesCategory).stream()
                .map(this::convertToBookWithRelations)
                .collect(Collectors.toList());
    }

    /**
     * Finds similar books and converts them to DTOs with relations.
     */
    public List<BookWithRelations> getSimilarBooksWithRelations(Long bookId) {
        return findSimilarBooks(bookId).stream()
                .map(this::convertToBookWithRelations)
                .collect(Collectors.toList());
    }
    
    /**
     * Updates the categories associated with a book.
     */
    @Transactional
    public Books updateBookCategories(Long bookId, List<Long> categoryIds) {
        if (bookId == null || bookId <= 0) {
            throw new ValidationException("Book ID must be a positive number");
        }
        
        Books book = getBookByIdOrThrow(bookId);
        
        // Clear existing categories
        book.getBookCategories().clear();
        
        // Add new categories if provided
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Long categoryId : categoryIds) {
                if (categoryId == null || categoryId <= 0) {
                    throw new ValidationException("Category ID must be a positive number");
                }
                
                Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId));
                
                BookCategory bookCategory = new BookCategory();
                bookCategory.setBook(book);
                bookCategory.setCategory(category);
                book.getBookCategories().add(bookCategory);
            }
        }
        
        try {
            return bookRepository.save(book);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to update book categories: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates the images associated with a book.
     */
    @Transactional
    public Books updateBookImages(Long bookId, List<BookImageRequest> imageRequests) {
        if (bookId == null || bookId <= 0) {
            throw new ValidationException("Book ID must be a positive number");
        }
        
        Books book = getBookByIdOrThrow(bookId);
        
        // Clear existing images
        book.getBookImages().clear();
        
        // Add new images if provided
        if (imageRequests != null && !imageRequests.isEmpty()) {
            for (BookImageRequest imageRequest : imageRequests) {
                if (imageRequest.getImageUrl() == null || imageRequest.getImageUrl().trim().isEmpty()) {
                    throw new ValidationException("Image URL is required");
                }
                
                BookImage bookImage = new BookImage();
                bookImage.setBook(book);
                bookImage.setImage(imageRequest.getImageUrl().trim());
                bookImage.setPrimary(imageRequest.getIsPrimary() != null ? imageRequest.getIsPrimary() : false);
                bookImage.setAltText(imageRequest.getAltText());
                bookImage.setDisplayOrder(imageRequest.getDisplayOrder() != null ? imageRequest.getDisplayOrder() : 0);
                
                book.getBookImages().add(bookImage);
            }
        }
        
        try {
            return bookRepository.save(book);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to update book images: " + e.getMessage(), e);
        }
    }
}