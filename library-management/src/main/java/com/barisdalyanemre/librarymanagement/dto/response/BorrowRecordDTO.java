package com.barisdalyanemre.librarymanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordDTO {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private boolean isOverdue;
}
