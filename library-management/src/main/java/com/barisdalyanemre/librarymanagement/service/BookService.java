package com.barisdalyanemre.librarymanagement.service;

import com.barisdalyanemre.librarymanagement.dto.BookDTO;
import com.barisdalyanemre.librarymanagement.dto.BookSearchRequest;
import com.barisdalyanemre.librarymanagement.dto.CreateBookRequest;
import com.barisdalyanemre.librarymanagement.dto.UpdateBookRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {

    /**
     * Creates a new book
     * @param request the book creation request
     * @return the created book
     */
    BookDTO createBook(CreateBookRequest request);
    
    /**
     * Gets a book by its ID
     * @param id the book ID
     * @return the book
     */
    BookDTO getBookById(Long id);
    
    /**
     * Gets a book by its ISBN
     * @param isbn the ISBN
     * @return the book
     */
    BookDTO getBookByIsbn(String isbn);
    
    /**
     * Gets all books with pagination
     * @param pageable pagination information
     * @return page of books
     */
    Page<BookDTO> getAllBooks(Pageable pageable);
    
    /**
     * Searches for books based on various criteria
     * @param request the search criteria
     * @return list of books matching criteria
     */
    List<BookDTO> searchBooks(BookSearchRequest request);
    
    /**
     * Updates a book
     * @param id the book ID
     * @param request the update request
     * @return the updated book
     */
    BookDTO updateBook(Long id, UpdateBookRequest request);
    
    /**
     * Deletes a book
     * @param id the book ID
     */
    void deleteBook(Long id);
    
    /**
     * Updates book availability status
     * @param id the book ID
     * @param available the availability status
     * @return the updated book
     */
    BookDTO updateBookAvailability(Long id, boolean available);
}
