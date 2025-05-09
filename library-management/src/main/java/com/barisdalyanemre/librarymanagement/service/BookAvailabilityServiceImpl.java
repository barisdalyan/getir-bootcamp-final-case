package com.barisdalyanemre.librarymanagement.service;

import com.barisdalyanemre.librarymanagement.event.BookAvailabilityEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
@Slf4j
public class BookAvailabilityServiceImpl implements BookAvailabilityService {

    private final Sinks.Many<BookAvailabilityEvent> availabilitySink;
    private final Flux<BookAvailabilityEvent> availabilityFlux;

    public BookAvailabilityServiceImpl() {
        // Create a sink that allows multiple subscribers and replays the last 100 events to new subscribers
        this.availabilitySink = Sinks.many().replay().limit(100);
        this.availabilityFlux = availabilitySink.asFlux().cache(100);
        
        log.info("BookAvailabilityService initialized");
    }

    @Override
    public void publishAvailabilityEvent(BookAvailabilityEvent event) {
        log.info("Publishing book availability event: {}", event);
        
        // Emit the event to all subscribers with retry if busy
        availabilitySink.emitNext(event, (signalType, emitResult) -> {
            if (emitResult.isFailure()) {
                log.error("Failed to emit book availability event: {}, result: {}", event, emitResult);
            }
            return false; // Don't retry
        });
    }

    @Override
    public Flux<BookAvailabilityEvent> getAvailabilityEventStream() {
        return availabilityFlux;
    }
}
