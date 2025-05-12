package com.barisdalyanemre.librarymanagement.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookRequest {

    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    @Size(min = 1, max = 255, message = "Author must be between 1 and 255 characters")
    private String author;

    @Size(max = 100, message = "Genre must be less than 100 characters")
    private String genre;
    
    private LocalDate publicationDate;
    
    private Boolean available;
}
