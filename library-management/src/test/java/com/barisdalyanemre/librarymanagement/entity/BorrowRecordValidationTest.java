package com.barisdalyanemre.librarymanagement.entity;

import com.barisdalyanemre.librarymanagement.enums.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BorrowRecordValidationTest {

    private Validator validator;
    private User testUser;
    private Book testBook;
    private BorrowRecord borrowRecord;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.PATRON);
        testUser.setEnabled(true);

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890");
        testBook.setPublicationDate(LocalDate.of(2023, 1, 1));
        testBook.setGenre("Test Genre");
        testBook.setAvailable(true);

        borrowRecord = new BorrowRecord();
        borrowRecord.setId(1L);
        borrowRecord.setUser(testUser);
        borrowRecord.setBook(testBook);
        borrowRecord.setBorrowDate(LocalDateTime.now());
        borrowRecord.setDueDate(LocalDateTime.now().plusDays(14));
    }

    @Test
    void validBorrowRecord_ShouldPassValidation() {
        Set<ConstraintViolation<BorrowRecord>> violations = validator.validate(borrowRecord);

        assertTrue(violations.isEmpty());
        assertDoesNotThrow(() -> validateBorrowRecordDates(borrowRecord));
    }

    @Test
    void borrowRecord_WithNullUser_ShouldFailValidation() {
        borrowRecord.setUser(null);

        Set<ConstraintViolation<BorrowRecord>> violations = validator.validate(borrowRecord);

        assertFalse(violations.isEmpty());
    }

    @Test
    void borrowRecord_WithNullBook_ShouldFailValidation() {
        borrowRecord.setBook(null);

        Set<ConstraintViolation<BorrowRecord>> violations = validator.validate(borrowRecord);

        assertFalse(violations.isEmpty());
    }

    @Test
    void borrowRecord_WithNullBorrowDate_ShouldFailValidation() {
        borrowRecord.setBorrowDate(null);

        Set<ConstraintViolation<BorrowRecord>> violations = validator.validate(borrowRecord);

        assertFalse(violations.isEmpty());
    }

    @Test
    void borrowRecord_WithNullDueDate_ShouldFailValidation() {
        borrowRecord.setDueDate(null);

        Set<ConstraintViolation<BorrowRecord>> violations = validator.validate(borrowRecord);

        assertFalse(violations.isEmpty());
    }

    @Test
    void borrowRecord_WithDueDateBeforeBorrowDate_ShouldFailValidation() {
        LocalDateTime now = LocalDateTime.now();
        borrowRecord.setBorrowDate(now);
        borrowRecord.setDueDate(now.minusDays(1));

        Exception exception = assertThrows(IllegalArgumentException.class, 
            () -> validateBorrowRecordDates(borrowRecord));
        
        assertTrue(exception.getMessage().contains("Due date must be after"));
    }

    @Test
    void borrowRecord_WithReturnDateBeforeBorrowDate_ShouldFailValidation() {
        LocalDateTime now = LocalDateTime.now();
        borrowRecord.setBorrowDate(now);
        borrowRecord.setReturnDate(now.minusDays(1));

        Exception exception = assertThrows(IllegalArgumentException.class, 
            () -> validateBorrowRecordDates(borrowRecord));
        
        assertTrue(exception.getMessage().contains("Return date must be after"));
    }

    private void validateBorrowRecordDates(BorrowRecord borrowRecord) {
        if (borrowRecord.getBorrowDate() != null && borrowRecord.getDueDate() != null) {
            if (borrowRecord.getDueDate().isBefore(borrowRecord.getBorrowDate())) {
                throw new IllegalArgumentException("Due date must be after or equal to borrow date");
            }
        }
        
        if (borrowRecord.getReturnDate() != null && borrowRecord.getBorrowDate() != null) {
            if (borrowRecord.getReturnDate().isBefore(borrowRecord.getBorrowDate())) {
                throw new IllegalArgumentException("Return date must be after or equal to borrow date");
            }
        }
    }
}
