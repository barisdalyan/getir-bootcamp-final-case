package com.barisdalyanemre.librarymanagement.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookAvailabilityEvent {
    private Long bookId;
    private String title;
    private String isbn;
    private boolean available;
    private LocalDateTime timestamp;
}
