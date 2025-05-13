package com.barisdalyanemre.librarymanagement.controller;

import com.barisdalyanemre.librarymanagement.dto.response.ApiError;
import com.barisdalyanemre.librarymanagement.dto.response.BorrowRecordDTO;
import com.barisdalyanemre.librarymanagement.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/borrow")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Book Borrowing", description = "APIs for borrowing and returning books")
@SecurityRequirement(name = "bearerAuth")
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping("/{bookId}")
    @Operation(summary = "Borrow a book", description = "Borrow a book by its ID. Available to all authenticated users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book borrowed successfully",
                    content = @Content(schema = @Schema(implementation = BorrowRecordDTO.class))),
            @ApiResponse(responseCode = "400", description = "Book is not available for borrowing",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Book not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "User has already borrowed this book",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<BorrowRecordDTO> borrowBook(@PathVariable Long bookId) {
        log.info("Request to borrow book with ID: {}", bookId);
        BorrowRecordDTO borrowRecord = borrowService.borrowBook(bookId);
        return ResponseEntity.status(HttpStatus.CREATED).body(borrowRecord);
    }

    @PutMapping("/return/{bookId}")
    @Operation(summary = "Return a book", description = "Return a borrowed book by its ID. Users can only return books they've borrowed, while librarians can return any book.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book returned successfully",
                    content = @Content(schema = @Schema(implementation = BorrowRecordDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot return a book borrowed by another user unless you're a librarian",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Book not found or not borrowed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Book is already returned",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<BorrowRecordDTO> returnBook(@PathVariable Long bookId) {
        log.info("Request to return book with ID: {}", bookId);
        BorrowRecordDTO borrowRecord = borrowService.returnBook(bookId);
        return ResponseEntity.ok(borrowRecord);
    }

    @GetMapping("/history")
    @Operation(summary = "Get user's borrowing history", description = "Get the borrowing history of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrowing history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<BorrowRecordDTO>> getUserBorrowHistory() {
        log.info("Request to get user's borrowing history");
        return ResponseEntity.ok(borrowService.getCurrentUserBorrowHistory());
    }

    @GetMapping("/active")
    @Operation(summary = "Get user's active loans", description = "Get the active loans of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active loans retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<BorrowRecordDTO>> getUserActiveLoans() {
        log.info("Request to get user's active loans");
        return ResponseEntity.ok(borrowService.getCurrentUserActiveLoans());
    }

    @GetMapping("/history/all")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get all borrowing records", description = "Get all borrowing records in the system. Only accessible by librarians.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All borrowing records retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires LIBRARIAN role",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<BorrowRecordDTO>> getAllBorrowRecords() {
        log.info("Request to get all borrowing records");
        return ResponseEntity.ok(borrowService.getAllBorrowRecords());
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get all overdue records", description = "Get all overdue borrowing records in the system. Only accessible by librarians.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue records retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires LIBRARIAN role",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<BorrowRecordDTO>> getAllOverdueRecords() {
        log.info("Request to get all overdue records");
        return ResponseEntity.ok(borrowService.getAllOverdueRecords());
    }

    @GetMapping("/overdue/report")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get overdue books report as text", description = "Get a formatted text report of all overdue books. Only accessible by librarians.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue report generated successfully",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires LIBRARIAN role",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<String> getOverdueReport() {
        log.info("Request to get overdue books report as text");
        
        String reportText = borrowService.generateOverdueReportText();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(reportText);
    }
}
