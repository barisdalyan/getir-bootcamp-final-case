package com.barisdalyanemre.librarymanagement.service;

import com.barisdalyanemre.librarymanagement.dto.BorrowRecordDTO;
import com.barisdalyanemre.librarymanagement.entity.Book;
import com.barisdalyanemre.librarymanagement.entity.BorrowRecord;
import com.barisdalyanemre.librarymanagement.entity.User;
import com.barisdalyanemre.librarymanagement.event.BookAvailabilityEvent;
import com.barisdalyanemre.librarymanagement.exception.BadRequestException;
import com.barisdalyanemre.librarymanagement.exception.ResourceNotFoundException;
import com.barisdalyanemre.librarymanagement.mapper.BorrowRecordMapper;
import com.barisdalyanemre.librarymanagement.repository.BookRepository;
import com.barisdalyanemre.librarymanagement.repository.BorrowRecordRepository;
import com.barisdalyanemre.librarymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRecordMapper borrowRecordMapper;
    private final BookAvailabilityService bookAvailabilityService;
    
    // Default loan period in days
    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;
    
    // Maximum active loans per user
    private static final int MAX_ACTIVE_LOANS = 5;

    @Override
    @Transactional
    public BorrowRecordDTO borrowBook(Long bookId) {
        // Get current authenticated user
        User user = getCurrentUser();
        
        // Check if user account is enabled
        if (!user.isEnabled()) {
            throw new BadRequestException("Your account is disabled. Cannot borrow books.");
        }
        
        // Check if user has reached maximum allowed active loans
        long activeLoansCount = borrowRecordRepository.countByUserAndReturnDateIsNull(user);
        if (activeLoansCount >= MAX_ACTIVE_LOANS) {
            throw new BadRequestException("You have reached the maximum limit of " + 
                                           MAX_ACTIVE_LOANS + " active loans");
        }
        
        // Find book by ID
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));
        
        // Check if book is available
        if (!book.getAvailable()) {
            throw new BadRequestException("Book is not available for borrowing");
        }
        
        // Check if user already has an active loan for this book
        if (borrowRecordRepository.existsByUserAndBookAndReturnDateIsNull(user, book)) {
            throw new BadRequestException("You already have an active loan for this book");
        }
        
        // Create new borrow record
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setUser(user);
        borrowRecord.setBook(book);
        borrowRecord.setBorrowDate(LocalDateTime.now());
        borrowRecord.setDueDate(LocalDateTime.now().plusDays(DEFAULT_LOAN_PERIOD_DAYS));
        
        // Update book availability
        book.setAvailable(false);
        bookRepository.save(book);
        
        // Publish book availability event
        publishAvailabilityEvent(book);
        
        // Save borrow record
        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);
        log.info("User {} borrowed book {}", user.getEmail(), book.getTitle());
        
        return borrowRecordMapper.toDTO(savedRecord);
    }

    @Override
    @Transactional
    public BorrowRecordDTO returnBook(Long bookId) {
        User user = getCurrentUser();
        
        // Check if user account is enabled
        if (!user.isEnabled()) {
            throw new BadRequestException("Your account is disabled. Please contact an administrator.");
        }
        
        // Find book by ID
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));
        
        // Find active borrow record for this book
        BorrowRecord borrowRecord = borrowRecordRepository.findByBookAndReturnDateIsNull(book)
                .orElseThrow(() -> new BadRequestException("No active loan found for this book"));
        
        // Ensure the user returning the book is the one who borrowed it or is a librarian
        if (!borrowRecord.getUser().getId().equals(user.getId()) && 
            !user.getRole().name().equals("LIBRARIAN")) {
            throw new BadRequestException("You can only return books that you borrowed");
        }
        
        // Update borrow record
        borrowRecord.setReturnDate(LocalDateTime.now());
        borrowRecordRepository.save(borrowRecord);
        
        // Update book availability
        book.setAvailable(true);
        bookRepository.save(book);
        
        // Publish book availability event
        publishAvailabilityEvent(book);
        
        log.info("Book {} returned by {}", book.getTitle(), user.getEmail());
        
        return borrowRecordMapper.toDTO(borrowRecord);
    }

    @Override
    public List<BorrowRecordDTO> getCurrentUserBorrowHistory() {
        User user = getCurrentUser();
        
        // Check if user account is enabled
        if (!user.isEnabled()) {
            throw new BadRequestException("Your account is disabled. Please contact an administrator.");
        }
        
        return borrowRecordRepository.findByUserOrderByBorrowDateDesc(user)
                .stream()
                .map(borrowRecordMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecordDTO> getCurrentUserActiveLoans() {
        User user = getCurrentUser();
        
        // Check if user account is enabled
        if (!user.isEnabled()) {
            throw new BadRequestException("Your account is disabled. Please contact an administrator.");
        }
        
        return borrowRecordRepository.findByUserAndReturnDateIsNullOrderByDueDateAsc(user)
                .stream()
                .map(borrowRecordMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecordDTO> getAllBorrowRecords() {
        return borrowRecordRepository.findAll()
                .stream()
                .map(borrowRecordMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecordDTO> getAllOverdueRecords() {
        return borrowRecordRepository.findAllOverdue(LocalDateTime.now())
                .stream()
                .map(borrowRecordMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional(readOnly = true)
    public void processOverdueBooks() {
        LocalDateTime now = LocalDateTime.now();
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findAllOverdue(now);
        
        log.info("Processing {} overdue books", overdueRecords.size());
        
        // Just log overdue books information
        for (BorrowRecord record : overdueRecords) {
            log.info("Overdue book: '{}' borrowed by {} {}, due date was {}", 
                    record.getBook().getTitle(), 
                    record.getUser().getFirstName(),
                    record.getUser().getLastName(),
                    record.getDueDate());
        }
    }
    
    @Override
    public byte[] generateOverdueReportCsv() {
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findAllOverdue(LocalDateTime.now());
        
        if (overdueRecords.isEmpty()) {
            return "No overdue books found".getBytes();
        }
        
        StringBuilder csvContent = new StringBuilder();
        // Add CSV headers
        csvContent.append("Book Title,ISBN,Patron First Name,Patron Last Name,Patron Email,Borrow Date,Due Date,Days Overdue\n");
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime now = LocalDateTime.now();
        
        for (BorrowRecord record : overdueRecords) {
            csvContent.append(escapeCsvField(record.getBook().getTitle())).append(",");
            csvContent.append(escapeCsvField(record.getBook().getIsbn())).append(",");
            csvContent.append(escapeCsvField(record.getUser().getFirstName())).append(",");
            csvContent.append(escapeCsvField(record.getUser().getLastName())).append(",");
            csvContent.append(escapeCsvField(record.getUser().getEmail())).append(",");
            csvContent.append(escapeCsvField(record.getBorrowDate().format(dateFormatter))).append(",");
            csvContent.append(escapeCsvField(record.getDueDate().format(dateFormatter))).append(",");
            
            // Calculate days overdue
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(record.getDueDate(), now);
            csvContent.append(daysOverdue).append("\n");
        }
        
        return csvContent.toString().getBytes();
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
    
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // If the field contains commas, quotes, or newlines, wrap it in quotes and escape any quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
    
    private void publishAvailabilityEvent(Book book) {
        BookAvailabilityEvent event = BookAvailabilityEvent.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .available(book.getAvailable())
                .timestamp(LocalDateTime.now())
                .build();
                
        bookAvailabilityService.publishAvailabilityEvent(event);
    }
}
