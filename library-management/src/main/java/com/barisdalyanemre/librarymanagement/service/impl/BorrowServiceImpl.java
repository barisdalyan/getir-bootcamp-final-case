package com.barisdalyanemre.librarymanagement.service.impl;

import com.barisdalyanemre.librarymanagement.dto.response.BorrowRecordDTO;
import com.barisdalyanemre.librarymanagement.entity.Book;
import com.barisdalyanemre.librarymanagement.entity.BorrowRecord;
import com.barisdalyanemre.librarymanagement.entity.User;
import com.barisdalyanemre.librarymanagement.event.BookAvailabilityEvent;
import com.barisdalyanemre.librarymanagement.exception.BadRequestException;
import com.barisdalyanemre.librarymanagement.exception.ConflictException;
import com.barisdalyanemre.librarymanagement.exception.ForbiddenException;
import com.barisdalyanemre.librarymanagement.exception.ResourceNotFoundException;
import com.barisdalyanemre.librarymanagement.mapper.BorrowRecordMapper;
import com.barisdalyanemre.librarymanagement.repository.BookRepository;
import com.barisdalyanemre.librarymanagement.repository.BorrowRecordRepository;
import com.barisdalyanemre.librarymanagement.repository.UserRepository;
import com.barisdalyanemre.librarymanagement.service.BookAvailabilityService;
import com.barisdalyanemre.librarymanagement.service.BorrowService;

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
        
        // Check if user has any overdue books
        if (borrowRecordRepository.hasOverdueBooks(user, LocalDateTime.now())) {
            throw new BadRequestException("You have overdue books. Please return them before borrowing more books.");
        }
        
        // Find book by ID
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        
        // Check if book is available
        if (!book.getAvailable()) {
            throw new ConflictException("Book is not available for borrowing");
        }
        
        // Check if user already has an active loan for this book
        if (borrowRecordRepository.existsByUserAndBookAndReturnDateIsNull(user, book)) {
            throw new ConflictException("You already have an active loan for this book");
        }
        
        // Create new borrow record
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setUser(user);
        borrowRecord.setBook(book);
        borrowRecord.setBorrowDate(LocalDateTime.now());
        borrowRecord.setDueDate(LocalDateTime.now().plusDays(DEFAULT_LOAN_PERIOD_DAYS));
        
        // Validate dates
        validateBorrowRecordDates(borrowRecord);
        
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
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        
        // Find active borrow record for this book
        BorrowRecord borrowRecord = borrowRecordRepository.findByBookAndReturnDateIsNull(book)
                .orElseThrow(() -> new ResourceNotFoundException("No active loan found for book with id: " + bookId));
        
        // Ensure the user returning the book is the one who borrowed it or is a librarian
        if (!borrowRecord.getUser().getId().equals(user.getId()) && 
            !user.getRole().name().equals("LIBRARIAN")) {
            throw new ForbiddenException("You can only return books that you borrowed");
        }
        
        // Update borrow record
        borrowRecord.setReturnDate(LocalDateTime.now());
        
        // Validate dates
        validateBorrowRecordDates(borrowRecord);
        
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
            throw new ForbiddenException("Your account is disabled. Please contact an administrator.");
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
            throw new ForbiddenException("Your account is disabled. Please contact an administrator.");
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
    public String generateOverdueReportText() {
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findAllOverdue(LocalDateTime.now());
        
        if (overdueRecords.isEmpty()) {
            return "LIBRARY OVERDUE BOOKS REPORT\n" +
                   "--------------------------\n" +
                   "No overdue books found\n\n" +
                   "Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        
        // Count books by days overdue
        LocalDateTime now = LocalDateTime.now();
        long lessThan7Days = overdueRecords.stream()
                .filter(r -> java.time.temporal.ChronoUnit.DAYS.between(r.getDueDate(), now) < 7)
                .count();
        long between7And14Days = overdueRecords.stream()
                .filter(r -> {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(r.getDueDate(), now);
                    return days >= 7 && days < 14;
                })
                .count();
        long between14And30Days = overdueRecords.stream()
                .filter(r -> {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(r.getDueDate(), now);
                    return days >= 14 && days < 30;
                })
                .count();
        long moreThan30Days = overdueRecords.stream()
                .filter(r -> java.time.temporal.ChronoUnit.DAYS.between(r.getDueDate(), now) >= 30)
                .count();
        
        // Get unique patrons with overdue books
        long uniquePatronsCount = overdueRecords.stream()
                .map(record -> record.getUser().getId())
                .distinct()
                .count();
        
        // Build the summary section
        StringBuilder report = new StringBuilder();
        report.append("LIBRARY OVERDUE BOOKS REPORT\n");
        report.append("--------------------------\n");
        report.append("Total Overdue Books: ").append(overdueRecords.size()).append('\n');
        report.append("Unique Patrons with Overdue Books: ").append(uniquePatronsCount).append('\n');
        report.append('\n');
        report.append("OVERDUE BREAKDOWN:\n");
        report.append("< 7 days overdue: ").append(lessThan7Days).append('\n');
        report.append("7-14 days overdue: ").append(between7And14Days).append('\n');
        report.append("14-30 days overdue: ").append(between14And30Days).append('\n');
        report.append("> 30 days overdue: ").append(moreThan30Days).append('\n');
        report.append('\n');
        
        // Add detailed overdue records section
        report.append("DETAILED OVERDUE RECORDS:\n");
        report.append("-----------------------\n");
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        int recordCount = 1;
        for (BorrowRecord record : overdueRecords) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(record.getDueDate(), now);
            
            report.append(recordCount++).append(". ");
            report.append("Book: \"").append(record.getBook().getTitle()).append("\" (ISBN: ").append(record.getBook().getIsbn()).append(")\n");
            report.append("   Author: ").append(record.getBook().getAuthor()).append('\n');
            report.append("   Patron: ").append(record.getUser().getFirstName()).append(" ").append(record.getUser().getLastName());
            report.append(" (").append(record.getUser().getEmail()).append(")\n");
            report.append("   Borrowed: ").append(record.getBorrowDate().format(dateFormatter)).append('\n');
            report.append("   Due: ").append(record.getDueDate().format(dateFormatter)).append('\n');
            report.append("   Days Overdue: ").append(daysOverdue).append('\n');
            report.append("\n");
        }
        
        report.append("\nGenerated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return report.toString();
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
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

    private void validateBorrowRecordDates(BorrowRecord borrowRecord) {
        // Validate due date is after borrow date
        if (borrowRecord.getBorrowDate() != null && borrowRecord.getDueDate() != null) {
            if (borrowRecord.getDueDate().isBefore(borrowRecord.getBorrowDate())) {
                throw new IllegalArgumentException("Due date must be after or equal to borrow date");
            }
        }
        
        // Validate return date is after borrow date (if return date exists)
        if (borrowRecord.getReturnDate() != null && borrowRecord.getBorrowDate() != null) {
            if (borrowRecord.getReturnDate().isBefore(borrowRecord.getBorrowDate())) {
                throw new IllegalArgumentException("Return date must be after or equal to borrow date");
            }
        }
    }
}
