package com.barisdalyanemre.librarymanagement.controller;

import com.barisdalyanemre.librarymanagement.dto.response.BorrowRecordDTO;
import com.barisdalyanemre.librarymanagement.entity.Book;
import com.barisdalyanemre.librarymanagement.entity.BorrowRecord;
import com.barisdalyanemre.librarymanagement.entity.User;
import com.barisdalyanemre.librarymanagement.enums.Role;
import com.barisdalyanemre.librarymanagement.repository.BookRepository;
import com.barisdalyanemre.librarymanagement.repository.BorrowRecordRepository;
import com.barisdalyanemre.librarymanagement.repository.UserRepository;
import com.barisdalyanemre.librarymanagement.security.JwtAuthenticationFilter;
import com.barisdalyanemre.librarymanagement.security.JwtUtils;
import com.barisdalyanemre.librarymanagement.service.BorrowService;
import com.barisdalyanemre.librarymanagement.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable Spring Security for tests
@ActiveProfiles("test")
class BorrowControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BorrowService borrowService;
    
    @MockBean
    private UserService userService;
    
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private BorrowRecordRepository borrowRecordRepository;
    
    @MockBean
    private JwtUtils jwtUtils;
    
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @MockBean
    private UserDetailsService userDetailsService;

    private User testUser;
    private User testLibrarian;
    private Book testBook;
    private Book testBook2;
    private BorrowRecord testBorrowRecord;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.PATRON);
        testUser.setEnabled(true);

        testLibrarian = new User();
        testLibrarian.setId(2L);
        testLibrarian.setFirstName("Test");
        testLibrarian.setLastName("Librarian");
        testLibrarian.setEmail("testlibrarian@example.com");
        testLibrarian.setPassword("password");
        testLibrarian.setRole(Role.LIBRARIAN);
        testLibrarian.setEnabled(true);

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890");
        testBook.setPublicationDate(LocalDate.of(2023, 1, 1));
        testBook.setGenre("Test Genre");
        testBook.setAvailable(true);

        testBook2 = new Book();
        testBook2.setId(2L);
        testBook2.setTitle("Test Book 2");
        testBook2.setAuthor("Test Author 2");
        testBook2.setIsbn("0987654321");
        testBook2.setPublicationDate(LocalDate.of(2023, 2, 1));
        testBook2.setGenre("Test Genre 2");
        testBook2.setAvailable(true);

        testBorrowRecord = new BorrowRecord();
        testBorrowRecord.setId(1L);
        testBorrowRecord.setUser(testUser);
        testBorrowRecord.setBook(testBook);
        testBorrowRecord.setBorrowDate(LocalDateTime.now().minusDays(7));
        testBorrowRecord.setDueDate(LocalDateTime.now().plusDays(7));
        
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(testLibrarian.getEmail())).thenReturn(Optional.of(testLibrarian));
        when(bookRepository.findById(testBook.getId())).thenReturn(Optional.of(testBook));
        when(bookRepository.findById(testBook2.getId())).thenReturn(Optional.of(testBook2));
    }

    @Test
    void borrowBook_Success() throws Exception {
        BorrowRecordDTO borrowRecordDTO = new BorrowRecordDTO();
        borrowRecordDTO.setId(1L);
        borrowRecordDTO.setBookId(testBook.getId());
        borrowRecordDTO.setUserId(testUser.getId());
        borrowRecordDTO.setBorrowDate(LocalDateTime.now());
        borrowRecordDTO.setDueDate(LocalDateTime.now().plusDays(14));
        
        when(borrowService.borrowBook(anyLong())).thenReturn(borrowRecordDTO);
        
        mockMvc.perform(post("/api/v1/borrow/{bookId}", testBook.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(testBook.getId()))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));
    }

    @Test
    void returnBook_Success() throws Exception {
        BorrowRecordDTO returnedRecordDTO = new BorrowRecordDTO();
        returnedRecordDTO.setId(1L);
        returnedRecordDTO.setBookId(testBook.getId());
        returnedRecordDTO.setUserId(testUser.getId());
        returnedRecordDTO.setBorrowDate(LocalDateTime.now().minusDays(7));
        returnedRecordDTO.setDueDate(LocalDateTime.now().plusDays(7));
        returnedRecordDTO.setReturnDate(LocalDateTime.now());
        
        when(borrowService.returnBook(anyLong())).thenReturn(returnedRecordDTO);
        
        mockMvc.perform(put("/api/v1/borrow/return/{bookId}", testBook.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(testBook.getId()))
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.returnDate").exists());
    }

    @Test
    void getUserBorrowHistory_Success() throws Exception {
        List<BorrowRecordDTO> borrowHistory = new ArrayList<>();
        BorrowRecordDTO record1 = new BorrowRecordDTO();
        record1.setId(1L);
        record1.setBookId(testBook.getId());
        record1.setUserId(testUser.getId());
        record1.setBorrowDate(LocalDateTime.now().minusDays(14));
        record1.setDueDate(LocalDateTime.now().minusDays(7));
        record1.setReturnDate(LocalDateTime.now().minusDays(8));
        borrowHistory.add(record1);
        
        when(borrowService.getCurrentUserBorrowHistory()).thenReturn(borrowHistory);
        
        mockMvc.perform(get("/api/v1/borrow/history")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(testBook.getId()))
                .andExpect(jsonPath("$[0].userId").value(testUser.getId()));
    }

    @Test
    void getUserActiveLoans_Success() throws Exception {
        List<BorrowRecordDTO> activeLoans = new ArrayList<>();
        BorrowRecordDTO record1 = new BorrowRecordDTO();
        record1.setId(1L);
        record1.setBookId(testBook.getId());
        record1.setUserId(testUser.getId());
        record1.setBorrowDate(LocalDateTime.now().minusDays(7));
        record1.setDueDate(LocalDateTime.now().plusDays(7));
        activeLoans.add(record1);
        
        when(borrowService.getCurrentUserActiveLoans()).thenReturn(activeLoans);
        
        mockMvc.perform(get("/api/v1/borrow/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(testBook.getId()))
                .andExpect(jsonPath("$[0].userId").value(testUser.getId()));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getAllBorrowRecords_Success() throws Exception {
        List<BorrowRecordDTO> allRecords = new ArrayList<>();
        BorrowRecordDTO record1 = new BorrowRecordDTO();
        record1.setId(1L);
        record1.setBookId(testBook.getId());
        record1.setUserId(testUser.getId());
        record1.setBorrowDate(LocalDateTime.now().minusDays(7));
        record1.setDueDate(LocalDateTime.now().plusDays(7));
        allRecords.add(record1);
        
        when(borrowService.getAllBorrowRecords()).thenReturn(allRecords);
        
        mockMvc.perform(get("/api/v1/borrow/history/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(testBook.getId()))
                .andExpect(jsonPath("$[0].userId").value(testUser.getId()));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getAllOverdueRecords_NoOverdueBooks() throws Exception {
        List<BorrowRecordDTO> overdueRecords = new ArrayList<>();
        
        when(borrowService.getAllOverdueRecords()).thenReturn(overdueRecords);
        
        mockMvc.perform(get("/api/v1/borrow/overdue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getOverdueReport_Success() throws Exception {
        when(borrowService.generateOverdueReportText()).thenReturn("Overdue Report Content");
        
        mockMvc.perform(get("/api/v1/borrow/overdue/report")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Overdue Report Content"));
    }
}
