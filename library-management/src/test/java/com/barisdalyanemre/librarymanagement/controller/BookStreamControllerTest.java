package com.barisdalyanemre.librarymanagement.controller;

import com.barisdalyanemre.librarymanagement.event.BookAvailabilityEvent;
import com.barisdalyanemre.librarymanagement.service.BookAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookStreamControllerTest {

    @Mock
    private BookAvailabilityService bookAvailabilityService;

    @InjectMocks
    private BookStreamController bookStreamController;

    private BookAvailabilityEvent testEvent;
    private Flux<BookAvailabilityEvent> testFlux;

    @BeforeEach
    void setUp() {
        testEvent = BookAvailabilityEvent.builder()
                .bookId(1L)
                .title("Test Book")
                .isbn("1234567890")
                .available(true)
                .timestamp(LocalDateTime.now())
                .build();
        
        testFlux = Flux.just(testEvent);
    }

    @Test
    void streamBookAvailability_Success() {
        when(bookAvailabilityService.getAvailabilityEventStream()).thenReturn(testFlux);

        Flux<BookAvailabilityEvent> result = bookStreamController.streamBookAvailability();

        StepVerifier.create(result)
                .expectNext(testEvent)
                .verifyComplete();
    }

    @Test
    void streamBookAvailability_MultipleEvents() {
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
        
        Flux<BookAvailabilityEvent> multipleEventsFlux = Flux.just(event1, event2);
        
        when(bookAvailabilityService.getAvailabilityEventStream()).thenReturn(multipleEventsFlux);

        Flux<BookAvailabilityEvent> result = bookStreamController.streamBookAvailability();

        StepVerifier.create(result)
                .expectNext(event1)
                .expectNext(event2)
                .verifyComplete();
    }
}
