package com.barisdalyanemre.librarymanagement.mapper;

import com.barisdalyanemre.librarymanagement.dto.request.CreateBookRequest;
import com.barisdalyanemre.librarymanagement.dto.request.UpdateBookRequest;
import com.barisdalyanemre.librarymanagement.dto.response.BookDTO;
import com.barisdalyanemre.librarymanagement.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookDTO toDTO(Book book) {
        return BookDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publicationDate(book.getPublicationDate())
                .genre(book.getGenre())
                .available(book.getAvailable())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
    
    public Book toEntity(CreateBookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublicationDate(request.getPublicationDate());
        book.setGenre(request.getGenre());
        book.setAvailable(true); // New books are available by default
        return book;
    }
    
    public void updateBookFromRequest(Book book, UpdateBookRequest request) {
        if (request.getTitle() != null) {
            book.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null) {
            book.setAuthor(request.getAuthor());
        }
        if (request.getGenre() != null) {
            book.setGenre(request.getGenre());
        }
        if (request.getPublicationDate() != null) {
            book.setPublicationDate(request.getPublicationDate());
        }
        if (request.getAvailable() != null) {
            book.setAvailable(request.getAvailable());
        }
        // ISBN is not updateable as it's a unique identifier
    }
}
