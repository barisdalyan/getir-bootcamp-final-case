package com.barisdalyanemre.librarymanagement.controller;

import com.barisdalyanemre.librarymanagement.dto.request.BookSearchRequest;
import com.barisdalyanemre.librarymanagement.dto.request.CreateBookRequest;
import com.barisdalyanemre.librarymanagement.dto.request.UpdateBookRequest;
import com.barisdalyanemre.librarymanagement.dto.response.BookDTO;
import com.barisdalyanemre.librarymanagement.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Book Management", description = "APIs for managing books")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;

    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Add a new book", description = "Create a new book entry. Only accessible to librarians.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Book created successfully",
                content = @Content(schema = @Schema(implementation = BookDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires LIBRARIAN role"),
        @ApiResponse(responseCode = "409", description = "Book with the same ISBN already exists")
    })
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody CreateBookRequest request) {
        log.info("Request to create a new book with ISBN: {}", request.getIsbn());
        BookDTO createdBook = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieve a book's details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book found",
                content = @Content(schema = @Schema(implementation = BookDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        log.info("Request to get book with ID: {}", id);
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Get book by ISBN", description = "Retrieve a book's details by ISBN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book found",
                content = @Content(schema = @Schema(implementation = BookDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<BookDTO> getBookByIsbn(@PathVariable String isbn) {
        log.info("Request to get book with ISBN: {}", isbn);
        return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
    }

    @GetMapping
    @Operation(
        summary = "Get all books with pagination", 
        description = "Retrieve a paginated list of books with optional sorting"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Books retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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
        summary = "Search books with pagination", 
        description = "Search for books based on various criteria like title, author, genre, availability, and publication date range with pagination support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<BookDTO>> searchBooks(
            @Parameter(description = "Title (partial match)") @RequestParam(required = false) String title,
            @Parameter(description = "Author (partial match)") @RequestParam(required = false) String author,
            @Parameter(description = "Genre (partial match)") @RequestParam(required = false) String genre,
            @Parameter(description = "Availability status") @RequestParam(required = false) Boolean available,
            @Parameter(description = "Published after date (YYYY-MM-DD)") @RequestParam(required = false) String publishedAfter,
            @Parameter(description = "Published before date (YYYY-MM-DD)") @RequestParam(required = false) String publishedBefore,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir
    ) {
        log.info("Request to search books with criteria: title={}, author={}, genre={}, available={}, page={}, size={}", 
                title, author, genre, available, page, size);
                
        BookSearchRequest searchRequest = BookSearchRequest.builder()
                .title(title)
                .author(author)
                .genre(genre)
                .available(available)
                .build();
                
        if (publishedAfter != null) {
            searchRequest.setPublishedAfter(java.time.LocalDate.parse(publishedAfter));
        }
        if (publishedBefore != null) {
            searchRequest.setPublishedBefore(java.time.LocalDate.parse(publishedBefore));
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(bookService.searchBooks(searchRequest, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Update book", description = "Update an existing book's details. Only accessible to librarians.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book updated successfully",
                content = @Content(schema = @Schema(implementation = BookDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires LIBRARIAN role"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "409", description = "Book with the same ISBN already exists")
    })
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @Valid @RequestBody UpdateBookRequest request) {
        log.info("Request to update book with ID: {}", id);
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Delete book", description = "Delete a book by ID. Only accessible to librarians.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires LIBRARIAN role"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "409", description = "Cannot delete book that is currently borrowed")
    })
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book availability updated successfully",
                content = @Content(schema = @Schema(implementation = BookDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires LIBRARIAN role"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<BookDTO> updateBookAvailability(
            @PathVariable Long id, 
            @Parameter(description = "Availability status (true/false)") @RequestParam boolean available
    ) {
        log.info("Request to update availability for book ID: {} to {}", id, available);
        return ResponseEntity.ok(bookService.updateBookAvailability(id, available));
    }
}
