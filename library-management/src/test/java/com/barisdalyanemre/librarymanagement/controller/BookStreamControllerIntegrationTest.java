package com.barisdalyanemre.librarymanagement.controller;

import com.barisdalyanemre.librarymanagement.entity.Book;
import com.barisdalyanemre.librarymanagement.entity.User;
import com.barisdalyanemre.librarymanagement.enums.Role;
import com.barisdalyanemre.librarymanagement.event.BookAvailabilityEvent;
import com.barisdalyanemre.librarymanagement.service.BookAvailabilityService;
import com.barisdalyanemre.librarymanagement.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookStreamControllerIntegrationTest {

    @Mock
    private BookAvailabilityService bookAvailabilityService;
    
    @Mock
    private BookService bookService;
    
    @InjectMocks
    private BookStreamController bookStreamController;
    
    private WebTestClient webTestClient;
    
    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
            .bindToController(bookStreamController)
            .build();
            
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("testuser@example.com");
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
    }

    @Test
    void streamBookAvailability_ReceivesPublishedEvents() {
        BookAvailabilityEvent event1 = BookAvailabilityEvent.builder()
            .bookId(testBook.getId())
            .title(testBook.getTitle())
            .isbn(testBook.getIsbn())
            .available(true)
            .timestamp(LocalDateTime.now())
            .build();
            
        BookAvailabilityEvent event2 = BookAvailabilityEvent.builder()
            .bookId(testBook.getId())
            .title(testBook.getTitle())
            .isbn(testBook.getIsbn())
            .available(false)
            .timestamp(LocalDateTime.now())
            .build();
        
        when(bookAvailabilityService.getAvailabilityEventStream())
            .thenReturn(Flux.just(event1, event2).delayElements(Duration.ofMillis(100)));
        
        webTestClient.get()
            .uri("/api/v1/books/stream")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .returnResult(BookAvailabilityEvent.class)
            .getResponseBody()
            .as(StepVerifier::create)
            .expectNext(event1)
            .expectNext(event2)
            .verifyComplete();
    }
}
