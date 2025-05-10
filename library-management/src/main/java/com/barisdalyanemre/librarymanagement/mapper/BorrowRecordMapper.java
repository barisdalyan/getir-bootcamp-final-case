package com.barisdalyanemre.librarymanagement.mapper;

import com.barisdalyanemre.librarymanagement.dto.BorrowRecordDTO;
import com.barisdalyanemre.librarymanagement.entity.BorrowRecord;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BorrowRecordMapper {

    public BorrowRecordDTO toDTO(BorrowRecord borrowRecord) {
        String userName = borrowRecord.getUser().getFirstName() + " " + borrowRecord.getUser().getLastName();
        
        return BorrowRecordDTO.builder()
                .id(borrowRecord.getId())
                .userId(borrowRecord.getUser().getId())
                .userName(userName)
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
