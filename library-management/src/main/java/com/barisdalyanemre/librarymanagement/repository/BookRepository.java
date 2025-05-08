package com.barisdalyanemre.librarymanagement.repository;

import com.barisdalyanemre.librarymanagement.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);
    
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    List<Book> findByGenreContainingIgnoreCase(String genre);
    List<Book> findByAvailable(Boolean available);
    
    @Query("SELECT b FROM Book b WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) " +
           "AND (:genre IS NULL OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))) " +
           "AND (:available IS NULL OR b.available = :available) " +
           "AND (:publishedAfter IS NULL OR b.publicationDate >= :publishedAfter) " +
           "AND (:publishedBefore IS NULL OR b.publicationDate <= :publishedBefore)")
    List<Book> searchBooks(
            @Param("title") String title,
            @Param("author") String author, 
            @Param("genre") String genre,
            @Param("available") Boolean available,
            @Param("publishedAfter") LocalDate publishedAfter,
            @Param("publishedBefore") LocalDate publishedBefore);
}
