package com.barisdalyanemre.librarymanagement.controller;

import com.barisdalyanemre.librarymanagement.event.BookAvailabilityEvent;
import com.barisdalyanemre.librarymanagement.service.BookAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Book Streaming", description = "APIs for streaming real-time book availability updates")
@SecurityRequirement(name = "bearerAuth")
public class BookStreamController {

    private final BookAvailabilityService bookAvailabilityService;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "Stream book availability updates", 
        description = "Returns a Server-Sent Events (SSE) stream of real-time book availability updates"
    )
    public Flux<BookAvailabilityEvent> streamBookAvailability() {
        log.info("Client subscribed to book availability stream");
        return bookAvailabilityService.getAvailabilityEventStream()
                .doOnCancel(() -> log.info("Client unsubscribed from book availability stream"))
                .doOnError(error -> log.error("Error in book availability stream", error));
    }
}
