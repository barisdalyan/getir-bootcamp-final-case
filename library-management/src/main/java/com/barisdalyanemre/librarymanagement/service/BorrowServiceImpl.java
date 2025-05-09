package com.barisdalyanemre.librarymanagement.service;

import com.barisdalyanemre.librarymanagement.dto.BorrowRecordDTO;
import com.barisdalyanemre.librarymanagement.entity.Book;
import com.barisdalyanemre.librarymanagement.entity.BorrowRecord;
import com.barisdalyanemre.librarymanagement.entity.User;
import com.barisdalyanemre.librarymanagement.exception.BadRequestException;
import com.barisdalyanemre.librarymanagement.exception.ResourceNotFoundException;
import com.barisdalyanemre.librarymanagement.mapper.BorrowRecordMapper;
import com.barisdalyanemre.librarymanagement.repository.BookRepository;
import com.barisdalyanemre.librarymanagement.repository.BorrowRecordRepository;
import com.barisdalyanemre.librarymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    
    // Default loan period in days
    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;
    
    // Maximum active loans per user
    private static final int MAX_ACTIVE_LOANS = 5;

    @Override
    @Transactional
    public BorrowRecordDTO borrowBook(Long bookId) {
        // Get current authenticated user
        User user = getCurrentUser();
        
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
        
        // Save borrow record
        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);
        log.info("User {} borrowed book {}", user.getEmail(), book.getTitle());
        
        return borrowRecordMapper.toDTO(savedRecord);
    }

    @Override
    @Transactional
    public BorrowRecordDTO returnBook(Long bookId) {
        User user = getCurrentUser();
        
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
        
        log.info("Book {} returned by {}", book.getTitle(), user.getEmail());
        
        return borrowRecordMapper.toDTO(borrowRecord);
    }

    @Override
    public List<BorrowRecordDTO> getCurrentUserBorrowHistory() {
        User user = getCurrentUser();
        
        return borrowRecordRepository.findByUserOrderByBorrowDateDesc(user)
                .stream()
                .map(borrowRecordMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecordDTO> getCurrentUserActiveLoans() {
        User user = getCurrentUser();
        
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
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
