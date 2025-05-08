package com.barisdalyanemre.librarymanagement.controller;

import com.barisdalyanemre.librarymanagement.dto.BookDTO;
import com.barisdalyanemre.librarymanagement.dto.BookSearchRequest;
import com.barisdalyanemre.librarymanagement.dto.CreateBookRequest;
import com.barisdalyanemre.librarymanagement.dto.UpdateBookRequest;
import com.barisdalyanemre.librarymanagement.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Book Management", description = "APIs for managing books")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;

    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Add a new book", description = "Create a new book entry. Only accessible to librarians.")
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody CreateBookRequest request) {
        log.info("Request to create a new book with ISBN: {}", request.getIsbn());
        BookDTO createdBook = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieve a book's details by ID")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        log.info("Request to get book with ID: {}", id);
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Get book by ISBN", description = "Retrieve a book's details by ISBN")
    public ResponseEntity<BookDTO> getBookByIsbn(@PathVariable String isbn) {
        log.info("Request to get book with ISBN: {}", isbn);
        return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
    }

    @GetMapping
    @Operation(
        summary = "Get all books with pagination", 
        description = "Retrieve a paginated list of books with optional sorting"
    )
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir
    ) {
        log.info("Request to get all books with pagination - page: {}, size: {}", page, size);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(bookService.getAllBooks(pageable));
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search books", 
        description = "Search for books based on various criteria like title, author, genre, availability, and publication date range"
    )
    public ResponseEntity<List<BookDTO>> searchBooks(
            @Parameter(description = "Title (partial match)") @RequestParam(required = false) String title,
            @Parameter(description = "Author (partial match)") @RequestParam(required = false) String author,
            @Parameter(description = "Genre (partial match)") @RequestParam(required = false) String genre,
            @Parameter(description = "Availability status") @RequestParam(required = false) Boolean available,
            @Parameter(description = "Published after date (YYYY-MM-DD)") @RequestParam(required = false) String publishedAfter,
            @Parameter(description = "Published before date (YYYY-MM-DD)") @RequestParam(required = false) String publishedBefore
    ) {
        log.info("Request to search books with criteria: title={}, author={}, genre={}, available={}", 
                title, author, genre, available);
                
        BookSearchRequest searchRequest = BookSearchRequest.builder()
                .title(title)
                .author(author)
                .genre(genre)
                .available(available)
                .build();
                
        // Parse dates if provided
        if (publishedAfter != null) {
            searchRequest.setPublishedAfter(java.time.LocalDate.parse(publishedAfter));
        }
        if (publishedBefore != null) {
            searchRequest.setPublishedBefore(java.time.LocalDate.parse(publishedBefore));
        }
        
        return ResponseEntity.ok(bookService.searchBooks(searchRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Update book", description = "Update an existing book's details. Only accessible to librarians.")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @Valid @RequestBody UpdateBookRequest request) {
        log.info("Request to update book with ID: {}", id);
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Delete book", description = "Delete a book by ID. Only accessible to librarians.")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.info("Request to delete book with ID: {}", id);
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(
        summary = "Update book availability", 
        description = "Update a book's availability status. Only accessible to librarians."
    )
    public ResponseEntity<BookDTO> updateBookAvailability(
            @PathVariable Long id, 
            @Parameter(description = "Availability status (true/false)") @RequestParam boolean available
    ) {
        log.info("Request to update availability for book ID: {} to {}", id, available);
        return ResponseEntity.ok(bookService.updateBookAvailability(id, available));
    }
}
