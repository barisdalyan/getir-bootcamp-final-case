package com.barisdalyanemre.librarymanagement.mapper;

import com.barisdalyanemre.librarymanagement.dto.response.BorrowRecordDTO;
import com.barisdalyanemre.librarymanagement.entity.BorrowRecord;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BorrowRecordMapper {

    public BorrowRecordDTO toDTO(BorrowRecord borrowRecord) {
        return BorrowRecordDTO.builder()
                .id(borrowRecord.getId())
                .userId(borrowRecord.getUser().getId())
                .firstName(borrowRecord.getUser().getFirstName())
                .lastName(borrowRecord.getUser().getLastName())
                .email(borrowRecord.getUser().getEmail())
                .bookId(borrowRecord.getBook().getId())
                .bookTitle(borrowRecord.getBook().getTitle())
                .bookIsbn(borrowRecord.getBook().getIsbn())
                .borrowDate(borrowRecord.getBorrowDate())
                .dueDate(borrowRecord.getDueDate())
                .returnDate(borrowRecord.getReturnDate())
                .isOverdue(borrowRecord.getReturnDate() == null && 
                           LocalDateTime.now().isAfter(borrowRecord.getDueDate()))
                .build();
    }
}
