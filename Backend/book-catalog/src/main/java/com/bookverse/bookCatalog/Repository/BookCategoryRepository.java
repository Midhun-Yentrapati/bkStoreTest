package com.bookverse.bookCatalog.Repository;

import com.bookverse.bookCatalog.Models.BookCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
    List<BookCategory> findByCategoryId(Long categoryId);
    List<BookCategory> findByBookId(Long bookId);
        
    @Transactional
    void deleteByBookIdAndCategoryId(Long bookId, Long categoryId);
    
    @Transactional
    void deleteByBookId(Long bookId);
}