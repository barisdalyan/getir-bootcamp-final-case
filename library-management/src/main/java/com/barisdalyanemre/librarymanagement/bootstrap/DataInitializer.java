package com.barisdalyanemre.librarymanagement.bootstrap;

import com.barisdalyanemre.librarymanagement.entity.Book;
import com.barisdalyanemre.librarymanagement.entity.Role;
import com.barisdalyanemre.librarymanagement.entity.User;
import com.barisdalyanemre.librarymanagement.repository.BookRepository;
import com.barisdalyanemre.librarymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
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
