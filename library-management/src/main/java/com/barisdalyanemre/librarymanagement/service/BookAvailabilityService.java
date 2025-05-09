package com.barisdalyanemre.librarymanagement.service;

import com.barisdalyanemre.librarymanagement.event.BookAvailabilityEvent;
import reactor.core.publisher.Flux;

public interface BookAvailabilityService {
    
    /**
     * Publishes a book availability event to subscribers
     * 
     * @param event the book availability event
     */
    void publishAvailabilityEvent(BookAvailabilityEvent event);
    
    /**
     * Returns a reactive stream of book availability events
     * 
     * @return flux of book availability events
     */
    Flux<BookAvailabilityEvent> getAvailabilityEventStream();
}
