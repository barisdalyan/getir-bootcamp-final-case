package com.barisdalyanemre.librarymanagement.service.impl;

import com.barisdalyanemre.librarymanagement.event.BookAvailabilityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class BookAvailabilityServiceImplTest {

    private BookAvailabilityServiceImpl bookAvailabilityService;
    private BookAvailabilityEvent testEvent;

    @BeforeEach
    void setUp() {
        bookAvailabilityService = new BookAvailabilityServiceImpl();
        
        testEvent = BookAvailabilityEvent.builder()
                .bookId(1L)
                .title("Test Book")
                .isbn("1234567890")
                .available(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void publishAvailabilityEvent_Success() {
        bookAvailabilityService.publishAvailabilityEvent(testEvent);
        
        Flux<BookAvailabilityEvent> eventStream = bookAvailabilityService.getAvailabilityEventStream();
        
        StepVerifier.create(eventStream.take(1))
                .expectNextMatches(event -> 
                    event.getBookId().equals(testEvent.getBookId()) &&
                    event.getTitle().equals(testEvent.getTitle()) &&
                    event.getIsbn().equals(testEvent.getIsbn()) &&
                    event.isAvailable() == testEvent.isAvailable()
                )
                .verifyComplete();
    }

    @Test
    void getAvailabilityEventStream_MultipleEvents() {
        BookAvailabilityEvent event1 = BookAvailabilityEvent.builder()
                .bookId(1L)
                .title("Book 1")
                .isbn("1111111111")
                .available(true)
                .timestamp(LocalDateTime.now())
                .build();
                
        BookAvailabilityEvent event2 = BookAvailabilityEvent.builder()
                .bookId(2L)
                .title("Book 2")
                .isbn("2222222222")
                .available(false)
                .timestamp(LocalDateTime.now())
                .build();
        
        bookAvailabilityService.publishAvailabilityEvent(event1);
        bookAvailabilityService.publishAvailabilityEvent(event2);
        
        Flux<BookAvailabilityEvent> eventStream = bookAvailabilityService.getAvailabilityEventStream();
        
        StepVerifier.create(eventStream.take(2))
                .expectNextMatches(event -> event.getBookId().equals(1L))
                .expectNextMatches(event -> event.getBookId().equals(2L))
                .verifyComplete();
    }

    @Test
    void getAvailabilityEventStream_ReplayEvents() {
        bookAvailabilityService.publishAvailabilityEvent(testEvent);
        
        Flux<BookAvailabilityEvent> eventStream = bookAvailabilityService.getAvailabilityEventStream();
        
        StepVerifier.create(eventStream.take(1))
                .expectNextMatches(event -> event.getBookId().equals(testEvent.getBookId()))
                .verifyComplete();
    }
}
