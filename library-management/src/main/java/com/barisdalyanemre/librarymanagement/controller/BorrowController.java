package com.barisdalyanemre.librarymanagement.controller;

import com.barisdalyanemre.librarymanagement.dto.BorrowRecordDTO;
import com.barisdalyanemre.librarymanagement.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Book Borrowing", description = "APIs for borrowing and returning books")
@SecurityRequirement(name = "bearerAuth")
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping("/{bookId}")
    @Operation(summary = "Borrow a book", description = "Borrow a book by its ID. Available to all authenticated users.")
    public ResponseEntity<BorrowRecordDTO> borrowBook(@PathVariable Long bookId) {
        log.info("Request to borrow book with ID: {}", bookId);
        BorrowRecordDTO borrowRecord = borrowService.borrowBook(bookId);
        return ResponseEntity.status(HttpStatus.CREATED).body(borrowRecord);
    }

    @PutMapping("/return/{bookId}")
    @Operation(summary = "Return a book", description = "Return a borrowed book by its ID. Users can only return books they've borrowed, while librarians can return any book.")
    public ResponseEntity<BorrowRecordDTO> returnBook(@PathVariable Long bookId) {
        log.info("Request to return book with ID: {}", bookId);
        BorrowRecordDTO borrowRecord = borrowService.returnBook(bookId);
        return ResponseEntity.ok(borrowRecord);
    }

    @GetMapping("/history")
    @Operation(summary = "Get user's borrowing history", description = "Get the borrowing history of the currently authenticated user.")
    public ResponseEntity<List<BorrowRecordDTO>> getUserBorrowHistory() {
        log.info("Request to get user's borrowing history");
        return ResponseEntity.ok(borrowService.getCurrentUserBorrowHistory());
    }

    @GetMapping("/active")
    @Operation(summary = "Get user's active loans", description = "Get the active loans of the currently authenticated user.")
    public ResponseEntity<List<BorrowRecordDTO>> getUserActiveLoans() {
        log.info("Request to get user's active loans");
        return ResponseEntity.ok(borrowService.getCurrentUserActiveLoans());
    }

    @GetMapping("/history/all")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get all borrowing records", description = "Get all borrowing records in the system. Only accessible by librarians.")
    public ResponseEntity<List<BorrowRecordDTO>> getAllBorrowRecords() {
        log.info("Request to get all borrowing records");
        return ResponseEntity.ok(borrowService.getAllBorrowRecords());
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get all overdue records", description = "Get all overdue borrowing records in the system. Only accessible by librarians.")
    public ResponseEntity<List<BorrowRecordDTO>> getAllOverdueRecords() {
        log.info("Request to get all overdue records");
        return ResponseEntity.ok(borrowService.getAllOverdueRecords());
    }

    @GetMapping("/overdue/download")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Download overdue books report as CSV", description = "Download a CSV report of all overdue books. Only accessible by librarians.")
    public ResponseEntity<byte[]> downloadOverdueReport() {
        log.info("Request to download overdue books report as CSV");
        
        byte[] csvContent = borrowService.generateOverdueReportCsv();
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "overdue_books_" + timestamp + ".csv";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(csvContent.length)
                .body(csvContent);
    }
}
