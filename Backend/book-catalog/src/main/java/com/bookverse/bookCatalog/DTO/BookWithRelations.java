package com.bookverse.bookCatalog.DTO;

import com.bookverse.bookCatalog.Models.Books;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookWithRelations {
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private String description;
    private String language;
    private String format;
    private String edition;
    private String publisher;
    private LocalDateTime publicationDate;
    private Integer pages;
    private Double weight;
    private String dimensions;
    private Double price;
    private Double mrp;
    private Integer stockDisplay;
    private Integer stockActual;
    private Integer noOfBooksSold;
    private Double totalRevenue;
    private Double averageRating;
    private Integer reviewCount;
    
    private Books.SalesCategory salesCategory;
    private Boolean isActive;
    private Boolean isFeatured;
    private LocalDateTime lastSoldAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<CategoryInfo> categories;
    private List<BookImageInfo> images;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String slug;
        private String description;
        private String image;
        private Boolean isActive;
        private Integer priority;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookImageInfo {
        private Long id;
        private String imageUrl;
        private String altText;
        private Boolean isPrimary;
    }
}