package com.barisdalyanemre.librarymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchRequest {
    private String title;
    private String author;
    private String genre;
    private Boolean available;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate publishedAfter;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate publishedBefore;
}
