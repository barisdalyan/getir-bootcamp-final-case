package com.barisdalyanemre.librarymanagement.controller;

import com.barisdalyanemre.librarymanagement.dto.response.BorrowRecordDTO;
import com.barisdalyanemre.librarymanagement.service.BorrowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowControllerTest {

    @Mock
    private BorrowService borrowService;

    @InjectMocks
    private BorrowController borrowController;

    private BorrowRecordDTO testBorrowRecordDTO;
    private List<BorrowRecordDTO> testBorrowRecordDTOList;

    @BeforeEach
    void setUp() {
        testBorrowRecordDTO = new BorrowRecordDTO();
        testBorrowRecordDTO.setId(1L);
        testBorrowRecordDTO.setUserId(1L);
        testBorrowRecordDTO.setFirstName("Test");
        testBorrowRecordDTO.setLastName("User");
        testBorrowRecordDTO.setEmail("user@example.com");
        testBorrowRecordDTO.setBookId(1L);
        testBorrowRecordDTO.setBookTitle("Test Book");
        testBorrowRecordDTO.setBookIsbn("1234567890");
        testBorrowRecordDTO.setBorrowDate(LocalDateTime.now());
        testBorrowRecordDTO.setDueDate(LocalDateTime.now().plusDays(14));

        testBorrowRecordDTOList = Arrays.asList(testBorrowRecordDTO);
    }

    @Test
    void borrowBook_Success() {
        when(borrowService.borrowBook(anyLong())).thenReturn(testBorrowRecordDTO);

        ResponseEntity<BorrowRecordDTO> response = borrowController.borrowBook(1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(borrowService).borrowBook(1L);
    }

    @Test
    void returnBook_Success() {
        when(borrowService.returnBook(anyLong())).thenReturn(testBorrowRecordDTO);

        ResponseEntity<BorrowRecordDTO> response = borrowController.returnBook(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(borrowService).returnBook(1L);
    }

    @Test
    void getUserBorrowHistory_Success() {
        when(borrowService.getCurrentUserBorrowHistory()).thenReturn(testBorrowRecordDTOList);

        ResponseEntity<List<BorrowRecordDTO>> response = borrowController.getUserBorrowHistory();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(borrowService).getCurrentUserBorrowHistory();
    }

    @Test
    void getUserActiveLoans_Success() {
        when(borrowService.getCurrentUserActiveLoans()).thenReturn(testBorrowRecordDTOList);

        ResponseEntity<List<BorrowRecordDTO>> response = borrowController.getUserActiveLoans();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(borrowService).getCurrentUserActiveLoans();
    }

    @Test
    void getAllBorrowRecords_Success() {
        when(borrowService.getAllBorrowRecords()).thenReturn(testBorrowRecordDTOList);

        ResponseEntity<List<BorrowRecordDTO>> response = borrowController.getAllBorrowRecords();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(borrowService).getAllBorrowRecords();
    }

    @Test
    void getAllOverdueRecords_Success() {
        when(borrowService.getAllOverdueRecords()).thenReturn(testBorrowRecordDTOList);

        ResponseEntity<List<BorrowRecordDTO>> response = borrowController.getAllOverdueRecords();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(borrowService).getAllOverdueRecords();
    }

    @Test
    void getOverdueReport_Success() {
        String reportText = "Test Report";
        when(borrowService.generateOverdueReportText()).thenReturn(reportText);

        ResponseEntity<String> response = borrowController.getOverdueReport();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(reportText, response.getBody());
        
        HttpHeaders headers = response.getHeaders();
        assertEquals(MediaType.TEXT_PLAIN, headers.getContentType());
        
        verify(borrowService).generateOverdueReportText();
    }
}
