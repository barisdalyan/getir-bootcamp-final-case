package com.barisdalyanemre.librarymanagement.bootstrap;

import com.barisdalyanemre.librarymanagement.entity.Book;
import com.barisdalyanemre.librarymanagement.entity.BorrowRecord;
import com.barisdalyanemre.librarymanagement.entity.Role;
import com.barisdalyanemre.librarymanagement.entity.User;
import com.barisdalyanemre.librarymanagement.repository.BookRepository;
import com.barisdalyanemre.librarymanagement.repository.BorrowRecordRepository;
import com.barisdalyanemre.librarymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_FIRST_NAME}")
    private String adminFirstName;

    @Value("${ADMIN_LAST_NAME}")
    private String adminLastName;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");

        // Create admin user if not exists
        if (userRepository.count() == 0) {
            createAdminUser();
            createSamplePatrons();
            createSampleBooks();
            createSampleBorrowRecords();
            log.info("Sample data created successfully");
        } else {
            log.info("Database already contains data, skipping initialization");
        }
    }

    private void createAdminUser() {
        User admin = new User();
        admin.setFirstName(adminFirstName);
        admin.setLastName(adminLastName);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.LIBRARIAN);
        admin.setContactDetails("Library Administrator");
        admin.setEnabled(true);

        userRepository.save(admin);
        log.info("Admin user created: {}", admin.getEmail());
    }

    private void createSamplePatrons() {
        // Create sample patrons
        List<User> patrons = Arrays.asList(
            createPatron("John", "Doe", "john@example.com", "password123", "123 Main St"),
            createPatron("Jane", "Smith", "jane@example.com", "password456", "456 Oak Ave"),
            createPatron("Bob", "Johnson", "bob@example.com", "password789", "789 Pine Blvd")
        );

        userRepository.saveAll(patrons);
        log.info("Created {} sample patrons", patrons.size());
    }
    
    private void createSampleBooks() {
        List<Book> books = Arrays.asList(
            createBook("To Kill a Mockingbird", "Harper Lee", "978-0446310789", 
                    LocalDate.of(1960, 7, 11), "Classic Fiction"),
            createBook("1984", "George Orwell", "978-0451524935", 
                    LocalDate.of(1949, 6, 8), "Dystopian Fiction"),
            createBook("The Great Gatsby", "F. Scott Fitzgerald", "978-0743273565", 
                    LocalDate.of(1925, 4, 10), "Classic Fiction"),
            createBook("Pride and Prejudice", "Jane Austen", "978-0141439518", 
                    LocalDate.of(1813, 1, 28), "Romance"),
            createBook("The Catcher in the Rye", "J.D. Salinger", "978-0316769488", 
                    LocalDate.of(1951, 7, 16), "Coming-of-age Fiction")
        );
        
        bookRepository.saveAll(books);
        log.info("Created {} sample books", books.size());
    }

    private void createSampleBorrowRecords() {
        List<User> users = userRepository.findAll();
        List<Book> books = bookRepository.findAll();
        List<BorrowRecord> borrowRecords = new ArrayList<>();
        
        if (users.size() < 3 || books.size() < 5) {
            log.warn("Not enough users or books to create sample borrow records");
            return;
        }
        
        // Get users by index assuming we have at least 3 patrons from createSamplePatrons()
        User user1 = users.stream()
                .filter(user -> user.getEmail().equals("john@example.com"))
                .findFirst()
                .orElse(users.get(1));
        
        User user2 = users.stream()
                .filter(user -> user.getEmail().equals("jane@example.com"))
                .findFirst()
                .orElse(users.get(2));
                
        User user3 = users.stream()
                .filter(user -> user.getEmail().equals("bob@example.com"))
                .findFirst()
                .orElse(users.get(3));
        
        // Currently borrowed, not overdue
        BorrowRecord record1 = new BorrowRecord();
        record1.setUser(user1);
        record1.setBook(books.get(0));  // To Kill a Mockingbird
        record1.setBorrowDate(LocalDateTime.now().minusDays(5));
        record1.setDueDate(LocalDateTime.now().plusDays(9)); // Due in 9 days
        record1.setReturnDate(null); // Not returned yet
        borrowRecords.add(record1);
        
        // Set book as not available since it's borrowed
        books.get(0).setAvailable(false);
        
        // Currently borrowed, overdue
        BorrowRecord record2 = new BorrowRecord();
        record2.setUser(user1);
        record2.setBook(books.get(1)); // 1984
        record2.setBorrowDate(LocalDateTime.now().minusDays(20));
        record2.setDueDate(LocalDateTime.now().minusDays(6)); // Overdue by 6 days
        record2.setReturnDate(null); // Not returned yet
        borrowRecords.add(record2);
        
        // Set book as not available since it's borrowed
        books.get(1).setAvailable(false);
        
        // Another overdue book
        BorrowRecord record3 = new BorrowRecord();
        record3.setUser(user2);
        record3.setBook(books.get(2)); // The Great Gatsby
        record3.setBorrowDate(LocalDateTime.now().minusDays(30));
        record3.setDueDate(LocalDateTime.now().minusDays(16)); // Overdue by 16 days
        record3.setReturnDate(null); // Not returned yet
        borrowRecords.add(record3);
        
        // Set book as not available since it's borrowed
        books.get(2).setAvailable(false);
        
        // Returned book (on time)
        BorrowRecord record4 = new BorrowRecord();
        record4.setUser(user2);
        record4.setBook(books.get(3)); // Pride and Prejudice
        record4.setBorrowDate(LocalDateTime.now().minusDays(25));
        record4.setDueDate(LocalDateTime.now().minusDays(11));
        record4.setReturnDate(LocalDateTime.now().minusDays(12)); // Returned 1 day before due
        borrowRecords.add(record4);
        
        // Returned book (late)
        BorrowRecord record5 = new BorrowRecord();
        record5.setUser(user3);
        record5.setBook(books.get(4)); // The Catcher in the Rye
        record5.setBorrowDate(LocalDateTime.now().minusDays(35));
        record5.setDueDate(LocalDateTime.now().minusDays(21));
        record5.setReturnDate(LocalDateTime.now().minusDays(15)); // Returned 6 days after due
        borrowRecords.add(record5);
        
        // Save updated books
        bookRepository.saveAll(books);
        
        // Save borrow records
        borrowRecordRepository.saveAll(borrowRecords);
        log.info("Created {} sample borrow records", borrowRecords.size());
    }

    private User createPatron(String firstName, String lastName, String email, String password, String contactDetails) {
        User patron = new User();
        patron.setFirstName(firstName);
        patron.setLastName(lastName);
        patron.setEmail(email);
        patron.setPassword(passwordEncoder.encode(password));
        patron.setRole(Role.PATRON);
        patron.setContactDetails(contactDetails);
        patron.setEnabled(true);
        return patron;
    }
    
    private Book createBook(String title, String author, String isbn, LocalDate publicationDate, String genre) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setPublicationDate(publicationDate);
        book.setGenre(genre);
        book.setAvailable(true);
        return book;
    }
}
