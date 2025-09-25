package com.bookverse.bookCatalog.Repository;

import com.bookverse.bookCatalog.Models.Books;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Books, Long>, JpaSpecificationExecutor<Books> {

    List<Books> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String titleQuery, String authorQuery);

    Optional<Books> findByIsbn(String isbn);

    List<Books> findBySalesCategory(Books.SalesCategory salesCategory);

    List<Books> findByBookCategoriesCategoryNameIn(List<String> categoryNames);

    // Finds all active books with their categories, alerts, and images eagerly loaded.
    @Query("SELECT DISTINCT b FROM Books b " +
           "LEFT JOIN FETCH b.bookCategories bc " +
           "LEFT JOIN FETCH bc.category " +
           "LEFT JOIN FETCH b.inventoryAlert " +
           "LEFT JOIN FETCH b.bookImages " +
           "WHERE b.isActive = true")
    List<Books> findAllWithCategories();

    // Finds a book by ID with its categories, alerts, and images eagerly loaded.
    @Query("SELECT b FROM Books b " +
           "LEFT JOIN FETCH b.bookCategories bc " +
           "LEFT JOIN FETCH bc.category " +
           "LEFT JOIN FETCH b.inventoryAlert " +
           "LEFT JOIN FETCH b.bookImages " +
           "WHERE b.id = :id")
    Optional<Books> findByIdWithCategories(@Param("id") Long id);

    // Finds active books by sales category with all relations eagerly loaded.
    @Query("SELECT DISTINCT b FROM Books b " +
           "LEFT JOIN FETCH b.bookCategories bc " +
           "LEFT JOIN FETCH bc.category " +
           "LEFT JOIN FETCH b.inventoryAlert " +
           "LEFT JOIN FETCH b.bookImages " +
           "WHERE b.salesCategory = :salesCategory AND b.isActive = true")
    List<Books> findBySalesCategoryWithRelations(@Param("salesCategory") Books.SalesCategory salesCategory);

    // Admin methods - finds all books (including inactive ones)
    @Query("SELECT DISTINCT b FROM Books b " +
           "LEFT JOIN FETCH b.bookCategories bc " +
           "LEFT JOIN FETCH bc.category " +
           "LEFT JOIN FETCH b.inventoryAlert " +
           "LEFT JOIN FETCH b.bookImages")
    List<Books> findAllWithCategoriesForAdmin();
}