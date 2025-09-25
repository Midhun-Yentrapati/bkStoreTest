package com.bookverse.bookCatalog.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "BOOKS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Books {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String isbn;
    private String title;
    private String author;
    
    @Column(length = 1000)
    private String description;
    
    private String language;
    private String format;
    private String edition;
    private String publisher;
    private LocalDateTime publicationDate;
    private int pages;
    private double weight;
    private String dimensions;
    private double price;
    private double mrp;
    private int stockDisplay;
    private int stockActual;
    private int noOfBooksSold = 0;
    private double totalRevenue = 0.0;
    private double averageRating = 0.0;
    private int reviewCount = 0;

    @Enumerated(EnumType.STRING)
    private SalesCategory salesCategory;

    private boolean isActive = true;
    private boolean isFeatured = false;
    private LocalDateTime lastSoldAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    // Unidirectional relationship to BookImage
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"book"})
    private Set<BookImage> bookImages = new HashSet<>();
    
    // Bidirectional relationship to BookCategory
    @JsonManagedReference("book-category")
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<BookCategory> bookCategories = new HashSet<>();
    
    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"book"})
    private InventoryAlert inventoryAlert;

    public enum SalesCategory {
        BEST_SELLING, SPECIAL_OFFERS, NEWLY_LAUNCHED
    }
    
    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? 
            ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? 
            ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Books books = (Books) o;
        return getId() != null && Objects.equals(getId(), books.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? 
            ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : 
            getClass().hashCode();
    }
}