package com.barisdalyanemre.librarymanagement.service;

import com.barisdalyanemre.librarymanagement.dto.BorrowRecordDTO;
import java.util.List;

public interface BorrowService {

    /**
     * Borrow a book for the currently authenticated user
     * 
     * @param bookId ID of the book to borrow
     * @return the borrow record details
     */
    BorrowRecordDTO borrowBook(Long bookId);
    
    /**
     * Return a borrowed book
     * 
     * @param bookId ID of the book to return
     * @return the updated borrow record details
     */
    BorrowRecordDTO returnBook(Long bookId);
    
    /**
     * Get borrowing history for the currently authenticated user
     * 
     * @return list of borrow records for the user
     */
    List<BorrowRecordDTO> getCurrentUserBorrowHistory();
    
    /**
     * Get active loans for the currently authenticated user
     * 
     * @return list of active borrow records for the user
     */
    List<BorrowRecordDTO> getCurrentUserActiveLoans();
    
    /**
     * Get all borrow records (only accessible by librarians)
     * 
     * @return list of all borrow records
     */
    List<BorrowRecordDTO> getAllBorrowRecords();
    
    /**
     * Get all overdue borrow records (only accessible by librarians)
     * 
     * @return list of overdue borrow records
     */
    List<BorrowRecordDTO> getAllOverdueRecords();
}
