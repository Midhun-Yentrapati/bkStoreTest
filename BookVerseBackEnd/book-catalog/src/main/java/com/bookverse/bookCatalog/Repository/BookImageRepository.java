package com.bookverse.bookCatalog.Repository;

import com.bookverse.bookCatalog.Models.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookImageRepository extends JpaRepository<BookImage, Long> {
    List<BookImage> findByBookIdOrderByDisplayOrderAsc(Long bookId);
}