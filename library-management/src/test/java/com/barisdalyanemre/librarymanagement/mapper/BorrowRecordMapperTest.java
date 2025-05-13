package com.barisdalyanemre.librarymanagement.mapper;

import com.barisdalyanemre.librarymanagement.dto.response.BorrowRecordDTO;
import com.barisdalyanemre.librarymanagement.entity.Book;
import com.barisdalyanemre.librarymanagement.entity.BorrowRecord;
import com.barisdalyanemre.librarymanagement.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BorrowRecordMapperTest {

    private BorrowRecordMapper borrowRecordMapper;
    private BorrowRecord borrowRecord;
    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        borrowRecordMapper = new BorrowRecordMapper();
        
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        
        book = new Book();
        book.setId(2L);
        book.setTitle("Test Book");
        book.setIsbn("1234567890");
        
        borrowRecord = new BorrowRecord();
        borrowRecord.setId(3L);
        borrowRecord.setUser(user);
        borrowRecord.setBook(book);
        borrowRecord.setBorrowDate(LocalDateTime.of(2023, 1, 1, 10, 0));
        borrowRecord.setDueDate(LocalDateTime.of(2023, 1, 15, 10, 0));
        borrowRecord.setReturnDate(null);
    }

    @Test
    void toDTO_shouldMapAllFields() {
        BorrowRecordDTO result = borrowRecordMapper.toDTO(borrowRecord);
        
        assertEquals(borrowRecord.getId(), result.getId());
        assertEquals(user.getId(), result.getUserId());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getLastName(), result.getLastName());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(book.getId(), result.getBookId());
        assertEquals(book.getTitle(), result.getBookTitle());
        assertEquals(book.getIsbn(), result.getBookIsbn());
        assertEquals(borrowRecord.getBorrowDate(), result.getBorrowDate());
        assertEquals(borrowRecord.getDueDate(), result.getDueDate());
        assertEquals(borrowRecord.getReturnDate(), result.getReturnDate());
    }

    @Test
    void toDTO_shouldCalculateOverdueStatusWhenNotReturned() {
        LocalDateTime pastDueDate = LocalDateTime.now().minusDays(5);
        borrowRecord.setDueDate(pastDueDate);
        borrowRecord.setReturnDate(null);
        
        BorrowRecordDTO result = borrowRecordMapper.toDTO(borrowRecord);
        
        assertTrue(result.isOverdue());
    }

    @Test
    void toDTO_shouldNotBeOverdueWhenReturnedLate() {
        LocalDateTime pastDueDate = LocalDateTime.now().minusDays(5);
        LocalDateTime returnDate = LocalDateTime.now().minusDays(2);
        borrowRecord.setDueDate(pastDueDate);
        borrowRecord.setReturnDate(returnDate);
        
        BorrowRecordDTO result = borrowRecordMapper.toDTO(borrowRecord);
        
        assertFalse(result.isOverdue());
    }

    @Test
    void toDTO_shouldNotBeOverdueWhenDueDateInFuture() {
        LocalDateTime futureDueDate = LocalDateTime.now().plusDays(5);
        borrowRecord.setDueDate(futureDueDate);
        borrowRecord.setReturnDate(null);
        
        BorrowRecordDTO result = borrowRecordMapper.toDTO(borrowRecord);
        
        assertFalse(result.isOverdue());
    }
}
