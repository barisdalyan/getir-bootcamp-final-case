package com.barisdalyanemre.librarymanagement.repository;

import com.barisdalyanemre.librarymanagement.entity.BorrowRecord;
import com.barisdalyanemre.librarymanagement.entity.User;
import com.barisdalyanemre.librarymanagement.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByUserOrderByBorrowDateDesc(User user);
    List<BorrowRecord> findByUserAndReturnDateIsNullOrderByDueDateAsc(User user);
   
    @Query("SELECT br FROM BorrowRecord br WHERE br.returnDate IS NULL AND br.dueDate < :now")
    List<BorrowRecord> findAllOverdue(@Param("now") LocalDateTime now);
   
    Optional<BorrowRecord> findByBookAndReturnDateIsNull(Book book);
    boolean existsByUserAndBookAndReturnDateIsNull(User user, Book book);
    long countByUserAndReturnDateIsNull(User user);
   
    @Query("SELECT br FROM BorrowRecord br WHERE br.borrowDate >= :startDate ORDER BY br.borrowDate DESC")
    List<BorrowRecord> findRecentBorrows(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT br FROM BorrowRecord br JOIN FETCH br.user JOIN FETCH br.book " +
           "WHERE br.returnDate IS NULL AND br.dueDate < :now ORDER BY br.dueDate ASC")
    List<BorrowRecord> findAllOverdueWithUserAndBookDetails(@Param("now") LocalDateTime now);
}
