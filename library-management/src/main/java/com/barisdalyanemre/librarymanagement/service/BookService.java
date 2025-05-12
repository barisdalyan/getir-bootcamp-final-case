package com.barisdalyanemre.librarymanagement.service;

import com.barisdalyanemre.librarymanagement.dto.request.BookSearchRequest;
import com.barisdalyanemre.librarymanagement.dto.request.CreateBookRequest;
import com.barisdalyanemre.librarymanagement.dto.request.UpdateBookRequest;
import com.barisdalyanemre.librarymanagement.dto.response.BookDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * Searches for books based on various criteria with pagination
     * @param request the search criteria
     * @param pageable pagination information
     * @return paginated results of books matching criteria
     */
    Page<BookDTO> searchBooks(BookSearchRequest request, Pageable pageable);
    
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
