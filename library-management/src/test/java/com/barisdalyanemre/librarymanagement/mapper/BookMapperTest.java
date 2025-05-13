package com.barisdalyanemre.librarymanagement.mapper;

import com.barisdalyanemre.librarymanagement.dto.request.CreateBookRequest;
import com.barisdalyanemre.librarymanagement.dto.request.UpdateBookRequest;
import com.barisdalyanemre.librarymanagement.dto.response.BookDTO;
import com.barisdalyanemre.librarymanagement.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookMapperTest {

    private BookMapper bookMapper;
    private Book book;
    private CreateBookRequest createBookRequest;
    private UpdateBookRequest updateBookRequest;

    @BeforeEach
    void setUp() {
        bookMapper = new BookMapper();
        
        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setPublicationDate(LocalDate.of(2020, 1, 1));
        book.setGenre("Fiction");
        book.setAvailable(true);
        book.setCreatedAt(LocalDateTime.of(2023, 1, 1, 0, 0));
        book.setUpdatedAt(LocalDateTime.of(2023, 1, 2, 0, 0));
        
        createBookRequest = new CreateBookRequest();
        createBookRequest.setTitle("New Book");
        createBookRequest.setAuthor("New Author");
        createBookRequest.setIsbn("0987654321");
        createBookRequest.setPublicationDate(LocalDate.of(2021, 2, 2));
        createBookRequest.setGenre("Non-Fiction");
        
        updateBookRequest = new UpdateBookRequest();
        updateBookRequest.setTitle("Updated Title");
        updateBookRequest.setAuthor("Updated Author");
        updateBookRequest.setGenre("Updated Genre");
        updateBookRequest.setPublicationDate(LocalDate.of(2022, 3, 3));
        updateBookRequest.setAvailable(false);
    }

    @Test
    void toDTO_shouldMapAllFields() {
        BookDTO result = bookMapper.toDTO(book);
        
        assertEquals(book.getId(), result.getId());
        assertEquals(book.getTitle(), result.getTitle());
        assertEquals(book.getAuthor(), result.getAuthor());
        assertEquals(book.getIsbn(), result.getIsbn());
        assertEquals(book.getPublicationDate(), result.getPublicationDate());
        assertEquals(book.getGenre(), result.getGenre());
        assertEquals(book.getAvailable(), result.getAvailable());
        assertEquals(book.getCreatedAt(), result.getCreatedAt());
        assertEquals(book.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void toEntity_shouldCreateBookFromRequest() {
        Book result = bookMapper.toEntity(createBookRequest);
        
        assertEquals(createBookRequest.getTitle(), result.getTitle());
        assertEquals(createBookRequest.getAuthor(), result.getAuthor());
        assertEquals(createBookRequest.getIsbn(), result.getIsbn());
        assertEquals(createBookRequest.getPublicationDate(), result.getPublicationDate());
        assertEquals(createBookRequest.getGenre(), result.getGenre());
        assertTrue(result.getAvailable());
        assertNull(result.getId());
    }

    @Test
    void updateBookFromRequest_shouldUpdateAllProvidedFields() {
        Book bookToUpdate = new Book();
        bookToUpdate.setTitle("Original Title");
        bookToUpdate.setAuthor("Original Author");
        bookToUpdate.setGenre("Original Genre");
        bookToUpdate.setPublicationDate(LocalDate.of(2019, 5, 5));
        bookToUpdate.setAvailable(true);
        
        bookMapper.updateBookFromRequest(bookToUpdate, updateBookRequest);
        
        assertEquals(updateBookRequest.getTitle(), bookToUpdate.getTitle());
        assertEquals(updateBookRequest.getAuthor(), bookToUpdate.getAuthor());
        assertEquals(updateBookRequest.getGenre(), bookToUpdate.getGenre());
        assertEquals(updateBookRequest.getPublicationDate(), bookToUpdate.getPublicationDate());
        assertEquals(updateBookRequest.getAvailable(), bookToUpdate.getAvailable());
    }

    @Test
    void updateBookFromRequest_shouldOnlyUpdateProvidedFields() {
        Book bookToUpdate = new Book();
        bookToUpdate.setTitle("Original Title");
        bookToUpdate.setAuthor("Original Author");
        bookToUpdate.setGenre("Original Genre");
        bookToUpdate.setPublicationDate(LocalDate.of(2019, 5, 5));
        bookToUpdate.setAvailable(true);
        
        UpdateBookRequest partialUpdate = new UpdateBookRequest();
        partialUpdate.setTitle("New Title");
        
        bookMapper.updateBookFromRequest(bookToUpdate, partialUpdate);
        
        assertEquals("New Title", bookToUpdate.getTitle());
        assertEquals("Original Author", bookToUpdate.getAuthor());
        assertEquals("Original Genre", bookToUpdate.getGenre());
        assertEquals(LocalDate.of(2019, 5, 5), bookToUpdate.getPublicationDate());
        assertTrue(bookToUpdate.getAvailable());
    }
}
