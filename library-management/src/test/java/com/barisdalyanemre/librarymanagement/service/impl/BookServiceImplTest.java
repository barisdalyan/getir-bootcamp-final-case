package com.barisdalyanemre.librarymanagement.service.impl;

import com.barisdalyanemre.librarymanagement.dto.request.BookSearchRequest;
import com.barisdalyanemre.librarymanagement.dto.request.CreateBookRequest;
import com.barisdalyanemre.librarymanagement.dto.request.UpdateBookRequest;
import com.barisdalyanemre.librarymanagement.dto.response.BookDTO;
import com.barisdalyanemre.librarymanagement.entity.Book;
import com.barisdalyanemre.librarymanagement.event.BookAvailabilityEvent;
import com.barisdalyanemre.librarymanagement.exception.BadRequestException;
import com.barisdalyanemre.librarymanagement.exception.ResourceNotFoundException;
import com.barisdalyanemre.librarymanagement.mapper.BookMapper;
import com.barisdalyanemre.librarymanagement.repository.BookRepository;
import com.barisdalyanemre.librarymanagement.service.BookAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private BookAvailabilityService bookAvailabilityService;

    @InjectMocks
    private BookServiceImpl bookService;

    @Captor
    private ArgumentCaptor<BookAvailabilityEvent> eventCaptor;

    private Book testBook;
    private BookDTO testBookDTO;
    private CreateBookRequest createBookRequest;
    private UpdateBookRequest updateBookRequest;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("9781234567897");
        testBook.setPublicationDate(LocalDate.of(2020, 1, 1));
        testBook.setGenre("Fiction");
        testBook.setAvailable(true);
        testBook.setCreatedAt(LocalDateTime.now());
        testBook.setUpdatedAt(LocalDateTime.now());

        testBookDTO = new BookDTO();
        testBookDTO.setId(1L);
        testBookDTO.setTitle("Test Book");
        testBookDTO.setAuthor("Test Author");
        testBookDTO.setIsbn("9781234567897");
        testBookDTO.setPublicationDate(LocalDate.of(2020, 1, 1));
        testBookDTO.setGenre("Fiction");
        testBookDTO.setAvailable(true);
        testBookDTO.setCreatedAt(LocalDateTime.now());
        testBookDTO.setUpdatedAt(LocalDateTime.now());

        createBookRequest = new CreateBookRequest();
        createBookRequest.setTitle("New Book");
        createBookRequest.setAuthor("New Author");
        createBookRequest.setIsbn("9789876543210");
        createBookRequest.setPublicationDate(LocalDate.of(2021, 2, 2));
        createBookRequest.setGenre("Non-fiction");

        updateBookRequest = new UpdateBookRequest();
        updateBookRequest.setTitle("Updated Book");
        updateBookRequest.setAuthor("Updated Author");
        updateBookRequest.setGenre("Updated Genre");
        updateBookRequest.setPublicationDate(LocalDate.of(2022, 3, 3));
        updateBookRequest.setAvailable(true);
    }

    @Test
    @DisplayName("Should create book successfully")
    void createBookSuccessfully() {
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookMapper.toEntity(any(CreateBookRequest.class))).thenReturn(testBook);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(testBookDTO);
        
        BookDTO result = bookService.createBook(createBookRequest);
        
        assertNotNull(result);
        assertEquals(testBookDTO, result);
        verify(bookAvailabilityService).publishAvailabilityEvent(eventCaptor.capture());
        
        BookAvailabilityEvent event = eventCaptor.getValue();
        assertEquals(testBook.getId(), event.getBookId());
        assertEquals(testBook.getTitle(), event.getTitle());
        assertEquals(testBook.getIsbn(), event.getIsbn());
        assertEquals(testBook.getAvailable(), event.isAvailable());
    }

    @Test
    @DisplayName("Should throw exception when ISBN already exists")
    void createBookWithExistingIsbn() {
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);
        
        assertThrows(BadRequestException.class, () -> bookService.createBook(createBookRequest));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should get book by ID")
    void getBookById() {
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));
        when(bookMapper.toDTO(any(Book.class))).thenReturn(testBookDTO);
        
        BookDTO result = bookService.getBookById(1L);
        
        assertNotNull(result);
        assertEquals(testBookDTO, result);
    }

    @Test
    @DisplayName("Should throw exception when book not found by ID")
    void getBookByIdNotFound() {
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(1L));
    }

    @Test
    @DisplayName("Should get book by ISBN")
    void getBookByIsbn() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(testBook));
        when(bookMapper.toDTO(any(Book.class))).thenReturn(testBookDTO);
        
        BookDTO result = bookService.getBookByIsbn("9781234567897");
        
        assertNotNull(result);
        assertEquals(testBookDTO, result);
    }

    @Test
    @DisplayName("Should throw exception when book not found by ISBN")
    void getBookByIsbnNotFound() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookByIsbn("9781234567897"));
    }

    @Test
    @DisplayName("Should get all books with pagination")
    void getAllBooks() {
        List<Book> books = new ArrayList<>();
        books.add(testBook);
        Page<Book> bookPage = new PageImpl<>(books);
        
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(testBookDTO);
        
        Page<BookDTO> result = bookService.getAllBooks(pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testBookDTO, result.getContent().get(0));
    }

    @Test
    @DisplayName("Should search books with criteria")
    void searchBooks() {
        List<Book> books = new ArrayList<>();
        books.add(testBook);
        Page<Book> bookPage = new PageImpl<>(books);
        
        BookSearchRequest searchRequest = new BookSearchRequest();
        searchRequest.setTitle("Test");
        searchRequest.setAuthor("Author");
        searchRequest.setGenre("Fiction");
        searchRequest.setAvailable(true);
        
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.searchBooks(
                anyString(), anyString(), anyString(), anyBoolean(), 
                any(), any(), any(Pageable.class))
        ).thenReturn(bookPage);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(testBookDTO);
        
        Page<BookDTO> result = bookService.searchBooks(searchRequest, pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testBookDTO, result.getContent().get(0));
    }

    @Test
    @DisplayName("Should update book successfully")
    void updateBook() {
        Book updatedBook = new Book();
        updatedBook.setId(1L);
        updatedBook.setTitle("Updated Book");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setGenre("Updated Genre");
        updatedBook.setAvailable(true);
        
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(testBookDTO);
        
        BookDTO result = bookService.updateBook(1L, updateBookRequest);
        
        assertNotNull(result);
        verify(bookMapper).updateBookFromRequest(any(Book.class), any(UpdateBookRequest.class));
        verify(bookRepository).save(any(Book.class));
        verify(bookAvailabilityService, never()).publishAvailabilityEvent(any());
    }

    @Test
    @DisplayName("Should publish event when availability changes during update")
    void updateBookWithAvailabilityChange() {
        testBook.setAvailable(false);
        updateBookRequest.setAvailable(true);
        
        Book updatedBook = new Book();
        updatedBook.setId(1L);
        updatedBook.setTitle("Updated Book");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setGenre("Updated Genre");
        updatedBook.setAvailable(true);
        
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(testBookDTO);
        
        BookDTO result = bookService.updateBook(1L, updateBookRequest);
        
        assertNotNull(result);
        verify(bookAvailabilityService).publishAvailabilityEvent(eventCaptor.capture());
        
        BookAvailabilityEvent event = eventCaptor.getValue();
        assertEquals(updatedBook.getId(), event.getBookId());
        assertTrue(event.isAvailable());
    }

    @Test
    @DisplayName("Should delete book successfully")
    void deleteBook() {
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));
        
        bookService.deleteBook(1L);
        
        verify(bookRepository).delete(testBook);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent book")
    void deleteBookNotFound() {
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> bookService.deleteBook(1L));
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    @DisplayName("Should update book availability")
    void updateBookAvailability() {
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));
        testBook.setAvailable(false);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(testBookDTO);
        
        BookDTO result = bookService.updateBookAvailability(1L, true);
        
        assertNotNull(result);
        verify(bookRepository).save(testBook);
        verify(bookAvailabilityService).publishAvailabilityEvent(any(BookAvailabilityEvent.class));
    }

    @Test
    @DisplayName("Should not publish event when availability doesn't change")
    void updateBookAvailabilityNoChange() {
        testBook.setAvailable(true);
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));
        when(bookMapper.toDTO(any(Book.class))).thenReturn(testBookDTO);
        
        BookDTO result = bookService.updateBookAvailability(1L, true);
        
        assertNotNull(result);
        verify(bookRepository, never()).save(any(Book.class));
        verify(bookAvailabilityService, never()).publishAvailabilityEvent(any());
    }
}
