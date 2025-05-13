package com.barisdalyanemre.librarymanagement.service.impl;

import com.barisdalyanemre.librarymanagement.dto.response.BorrowRecordDTO;
import com.barisdalyanemre.librarymanagement.entity.Book;
import com.barisdalyanemre.librarymanagement.entity.BorrowRecord;
import com.barisdalyanemre.librarymanagement.entity.User;
import com.barisdalyanemre.librarymanagement.enums.Role;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceImplTest {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowRecordMapper borrowRecordMapper;

    @Mock
    private BookAvailabilityService bookAvailabilityService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    private User testUser;
    private User librarian;
    private Book testBook;
    private BorrowRecord testBorrowRecord;
    private BorrowRecordDTO testBorrowRecordDTO;

    @BeforeEach
    void setUp() {
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.getName()).thenReturn("user@example.com");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password");
        testUser.setRole(Role.PATRON);
        testUser.setEnabled(true);

        librarian = new User();
        librarian.setId(2L);
        librarian.setEmail("librarian@example.com");
        librarian.setFirstName("Test");
        librarian.setLastName("Librarian");
        librarian.setPassword("password");
        librarian.setRole(Role.LIBRARIAN);
        librarian.setEnabled(true);

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890");
        testBook.setAvailable(true);

        testBorrowRecord = new BorrowRecord();
        testBorrowRecord.setId(1L);
        testBorrowRecord.setUser(testUser);
        testBorrowRecord.setBook(testBook);
        testBorrowRecord.setBorrowDate(LocalDateTime.now());
        testBorrowRecord.setDueDate(LocalDateTime.now().plusDays(14));

        testBorrowRecordDTO = new BorrowRecordDTO();
        testBorrowRecordDTO.setId(1L);
        testBorrowRecordDTO.setUserId(testUser.getId());
        testBorrowRecordDTO.setEmail(testUser.getEmail());
        testBorrowRecordDTO.setFirstName(testUser.getFirstName());
        testBorrowRecordDTO.setLastName(testUser.getLastName());
        testBorrowRecordDTO.setBookId(testBook.getId());
        testBorrowRecordDTO.setBookTitle(testBook.getTitle());
        testBorrowRecordDTO.setBookIsbn(testBook.getIsbn());
        testBorrowRecordDTO.setBorrowDate(testBorrowRecord.getBorrowDate());
        testBorrowRecordDTO.setDueDate(testBorrowRecord.getDueDate());

        lenient().when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        lenient().when(userRepository.findByEmail("librarian@example.com")).thenReturn(Optional.of(librarian));
    }

    @Test
    void borrowBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowRecordRepository.countByUserAndReturnDateIsNull(eq(testUser))).thenReturn(0L);
        when(borrowRecordRepository.hasOverdueBooks(eq(testUser), any(LocalDateTime.class))).thenReturn(false);
        when(borrowRecordRepository.existsByUserAndBookAndReturnDateIsNull(eq(testUser), eq(testBook))).thenReturn(false);
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(testBorrowRecord);
        when(borrowRecordMapper.toDTO(any(BorrowRecord.class))).thenReturn(testBorrowRecordDTO);

        BorrowRecordDTO result = borrowService.borrowBook(1L);

        assertNotNull(result);
        assertEquals(testBorrowRecordDTO.getId(), result.getId());
        assertEquals(testBorrowRecordDTO.getBookTitle(), result.getBookTitle());

        verify(bookRepository).save(testBook);
        assertFalse(testBook.getAvailable());

        verify(bookAvailabilityService).publishAvailabilityEvent(any(BookAvailabilityEvent.class));
    }

    @Test
    void borrowBook_BookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> borrowService.borrowBook(1L));
    }

    @Test
    void borrowBook_UserDisabled() {
        testUser.setEnabled(false);

        assertThrows(BadRequestException.class, () -> borrowService.borrowBook(1L));
    }

    @Test
    void borrowBook_MaxLoansReached() {
        when(borrowRecordRepository.countByUserAndReturnDateIsNull(eq(testUser))).thenReturn(5L);

        assertThrows(BadRequestException.class, () -> borrowService.borrowBook(1L));
    }

    @Test
    void borrowBook_HasOverdueBooks() {
        when(borrowRecordRepository.countByUserAndReturnDateIsNull(eq(testUser))).thenReturn(1L);
        when(borrowRecordRepository.hasOverdueBooks(eq(testUser), any(LocalDateTime.class))).thenReturn(true);

        assertThrows(BadRequestException.class, () -> borrowService.borrowBook(1L));
    }

    @Test
    void borrowBook_BookNotAvailable() {
        testBook.setAvailable(false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowRecordRepository.countByUserAndReturnDateIsNull(eq(testUser))).thenReturn(0L);
        when(borrowRecordRepository.hasOverdueBooks(eq(testUser), any(LocalDateTime.class))).thenReturn(false);

        assertThrows(ConflictException.class, () -> borrowService.borrowBook(1L));
    }

    @Test
    void borrowBook_AlreadyBorrowed() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowRecordRepository.countByUserAndReturnDateIsNull(eq(testUser))).thenReturn(0L);
        when(borrowRecordRepository.hasOverdueBooks(eq(testUser), any(LocalDateTime.class))).thenReturn(false);
        when(borrowRecordRepository.existsByUserAndBookAndReturnDateIsNull(eq(testUser), eq(testBook))).thenReturn(true);

        assertThrows(ConflictException.class, () -> borrowService.borrowBook(1L));
    }

    @Test
    void returnBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowRecordRepository.findByBookAndReturnDateIsNull(testBook)).thenReturn(Optional.of(testBorrowRecord));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(testBorrowRecord);
        when(borrowRecordMapper.toDTO(testBorrowRecord)).thenReturn(testBorrowRecordDTO);

        BorrowRecordDTO result = borrowService.returnBook(1L);

        assertNotNull(result);
        assertEquals(testBorrowRecordDTO.getId(), result.getId());
        
        verify(bookRepository).save(testBook);
        assertTrue(testBook.getAvailable());
        
        verify(borrowRecordRepository).save(testBorrowRecord);
        assertNotNull(testBorrowRecord.getReturnDate());
        
        verify(bookAvailabilityService).publishAvailabilityEvent(any(BookAvailabilityEvent.class));
    }

    @Test
    void returnBook_BookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> borrowService.returnBook(1L));
    }

    @Test
    void returnBook_NoActiveLoan() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowRecordRepository.findByBookAndReturnDateIsNull(testBook)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> borrowService.returnBook(1L));
    }

    @Test
    void returnBook_NotBorrower() {
        BorrowRecord otherUserRecord = new BorrowRecord();
        User otherUser = new User();
        otherUser.setId(3L);
        otherUserRecord.setUser(otherUser);
        otherUserRecord.setBook(testBook);
        
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowRecordRepository.findByBookAndReturnDateIsNull(testBook)).thenReturn(Optional.of(otherUserRecord));

        assertThrows(ForbiddenException.class, () -> borrowService.returnBook(1L));
    }

    @Test
    void returnBook_LibrarianCanReturnAnyBook() {
        when(authentication.getName()).thenReturn("librarian@example.com");
        
        BorrowRecord otherUserRecord = new BorrowRecord();
        User otherUser = new User();
        otherUser.setId(3L);
        otherUserRecord.setUser(otherUser);
        otherUserRecord.setBook(testBook);
        
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowRecordRepository.findByBookAndReturnDateIsNull(testBook)).thenReturn(Optional.of(otherUserRecord));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(otherUserRecord);
        when(borrowRecordMapper.toDTO(otherUserRecord)).thenReturn(testBorrowRecordDTO);

        BorrowRecordDTO result = borrowService.returnBook(1L);

        assertNotNull(result);
        
        verify(bookRepository).save(testBook);
        assertTrue(testBook.getAvailable());
    }

    @Test
    void getCurrentUserBorrowHistory_Success() {
        List<BorrowRecord> borrowRecords = new ArrayList<>();
        borrowRecords.add(testBorrowRecord);
        
        when(borrowRecordRepository.findByUserOrderByBorrowDateDesc(testUser)).thenReturn(borrowRecords);
        when(borrowRecordMapper.toDTO(testBorrowRecord)).thenReturn(testBorrowRecordDTO);

        List<BorrowRecordDTO> result = borrowService.getCurrentUserBorrowHistory();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBorrowRecordDTO.getId(), result.get(0).getId());
    }

    @Test
    void getCurrentUserActiveLoans_Success() {
        List<BorrowRecord> borrowRecords = new ArrayList<>();
        borrowRecords.add(testBorrowRecord);
        
        when(borrowRecordRepository.findByUserAndReturnDateIsNullOrderByDueDateAsc(testUser)).thenReturn(borrowRecords);
        when(borrowRecordMapper.toDTO(testBorrowRecord)).thenReturn(testBorrowRecordDTO);

        List<BorrowRecordDTO> result = borrowService.getCurrentUserActiveLoans();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBorrowRecordDTO.getId(), result.get(0).getId());
    }

    @Test
    void getAllBorrowRecords_Success() {
        List<BorrowRecord> borrowRecords = new ArrayList<>();
        borrowRecords.add(testBorrowRecord);
        
        when(borrowRecordRepository.findAll()).thenReturn(borrowRecords);
        when(borrowRecordMapper.toDTO(testBorrowRecord)).thenReturn(testBorrowRecordDTO);

        List<BorrowRecordDTO> result = borrowService.getAllBorrowRecords();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBorrowRecordDTO.getId(), result.get(0).getId());
    }

    @Test
    void getAllOverdueRecords_Success() {
        List<BorrowRecord> borrowRecords = new ArrayList<>();
        borrowRecords.add(testBorrowRecord);
        
        when(borrowRecordRepository.findAllOverdue(any(LocalDateTime.class))).thenReturn(borrowRecords);
        when(borrowRecordMapper.toDTO(testBorrowRecord)).thenReturn(testBorrowRecordDTO);

        List<BorrowRecordDTO> result = borrowService.getAllOverdueRecords();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBorrowRecordDTO.getId(), result.get(0).getId());
    }

    @Test
    void generateOverdueReportText_WithOverdueBooks() {
        List<BorrowRecord> borrowRecords = new ArrayList<>();
        borrowRecords.add(testBorrowRecord);
        
        when(borrowRecordRepository.findAllOverdue(any(LocalDateTime.class))).thenReturn(borrowRecords);

        String result = borrowService.generateOverdueReportText();

        assertNotNull(result);
        assertTrue(result.contains("LIBRARY OVERDUE BOOKS REPORT"));
        assertTrue(result.contains("Total Overdue Books: 1"));
        assertTrue(result.contains("Test Book"));
    }

    @Test
    void generateOverdueReportText_NoOverdueBooks() {
        when(borrowRecordRepository.findAllOverdue(any(LocalDateTime.class))).thenReturn(new ArrayList<>());

        String result = borrowService.generateOverdueReportText();

        assertNotNull(result);
        assertTrue(result.contains("LIBRARY OVERDUE BOOKS REPORT"));
        assertTrue(result.contains("No overdue books found"));
    }

    @Test
    void processOverdueBooks_Success() {
        List<BorrowRecord> borrowRecords = new ArrayList<>();
        borrowRecords.add(testBorrowRecord);
        
        when(borrowRecordRepository.findAllOverdue(any(LocalDateTime.class))).thenReturn(borrowRecords);

        borrowService.processOverdueBooks();

        verify(borrowRecordRepository).findAllOverdue(any(LocalDateTime.class));
    }
}
