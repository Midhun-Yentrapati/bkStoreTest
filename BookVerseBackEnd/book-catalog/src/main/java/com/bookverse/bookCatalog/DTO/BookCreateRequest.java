package com.bookverse.bookCatalog.DTO;

import com.bookverse.bookCatalog.Models.Books;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
public class BookCreateRequest {
    private String isbn;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Author is required")
    private String author;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String language;
    private String format;
    private String edition;
    private String publisher;
    private LocalDateTime publicationDate;
    private Integer pages;
    private Double weight;
    private String dimensions;
    
    @NotNull(message = "Price is required")
    private Double price;
    
    private Double mrp;
    
    @NotNull(message = "Display stock is required")
    private Integer stockDisplay;
    
    @NotNull(message = "Actual stock is required")
    private Integer stockActual;
    
    @NotNull(message = "Sales category is required")
    private Books.SalesCategory salesCategory;
    
    private Boolean isActive = true;
    private Boolean isFeatured = false;
    
    private List<Long> categoryIds;
    private List<BookImageRequest> images;

    
}