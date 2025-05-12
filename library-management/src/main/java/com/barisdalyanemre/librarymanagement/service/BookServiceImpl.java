package com.barisdalyanemre.librarymanagement.service;

import com.barisdalyanemre.librarymanagement.dto.BookDTO;
import com.barisdalyanemre.librarymanagement.dto.BookSearchRequest;
import com.barisdalyanemre.librarymanagement.dto.CreateBookRequest;
import com.barisdalyanemre.librarymanagement.dto.UpdateBookRequest;
import com.barisdalyanemre.librarymanagement.entity.Book;
import com.barisdalyanemre.librarymanagement.event.BookAvailabilityEvent;
import com.barisdalyanemre.librarymanagement.exception.BadRequestException;
import com.barisdalyanemre.librarymanagement.exception.ResourceNotFoundException;
import com.barisdalyanemre.librarymanagement.mapper.BookMapper;
import com.barisdalyanemre.librarymanagement.repository.BookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookAvailabilityService bookAvailabilityService;

    @Override
    @Transactional
    public BookDTO createBook(CreateBookRequest request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BadRequestException("A book with ISBN " + request.getIsbn() + " already exists");
        }

        Book book = bookMapper.toEntity(request);
        Book savedBook = bookRepository.save(book);
        log.info("Created new book with ISBN: {}", savedBook.getIsbn());
        
        // Publish availability event for the new book
        publishAvailabilityEvent(savedBook);
        
        return bookMapper.toDTO(savedBook);
    }

    @Override
    public BookDTO getBookById(Long id) {
        Book book = findBookById(id);
        return bookMapper.toDTO(book);
    }

    @Override
    public BookDTO getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with isbn: " + isbn));
        return bookMapper.toDTO(book);
    }

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(bookMapper::toDTO);
    }
    
    @Override
    public Page<BookDTO> searchBooks(BookSearchRequest request, Pageable pageable) {
        return bookRepository.searchBooks(
                request.getTitle(),
                request.getAuthor(),
                request.getGenre(),
                request.getAvailable(),
                request.getPublishedAfter(),
                request.getPublishedBefore(),
                pageable
            ).map(bookMapper::toDTO);
    }

    @Override
    @Transactional
    public BookDTO updateBook(Long id, UpdateBookRequest request) {
        Book book = findBookById(id);
        
        // Store previous availability state
        boolean previousAvailability = book.getAvailable();
        
        bookMapper.updateBookFromRequest(book, request);
        Book updatedBook = bookRepository.save(book);
        log.info("Updated book with ID: {}", id);
        
        // If availability changed, publish an event
        if (previousAvailability != updatedBook.getAvailable()) {
            publishAvailabilityEvent(updatedBook);
        }
        
        return bookMapper.toDTO(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = findBookById(id);
        bookRepository.delete(book);
        log.info("Deleted book with ID: {}", id);
    }

    @Override
    @Transactional
    public BookDTO updateBookAvailability(Long id, boolean available) {
        Book book = findBookById(id);
        
        // Only publish event if availability actually changed
        if (book.getAvailable() != available) {
            book.setAvailable(available);
            Book updatedBook = bookRepository.save(book);
            log.info("Updated availability for book ID: {} to {}", id, available);
            
            publishAvailabilityEvent(updatedBook);
        }
        
        return bookMapper.toDTO(book);
    }
    
    private Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
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
