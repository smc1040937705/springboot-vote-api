package com.vote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateVoteRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotEmpty(message = "At least one option is required")
    private List<@NotBlank(message = "Option content cannot be blank") String> options;
    
    @NotNull(message = "Deadline is required")
    private LocalDateTime deadline;
    
    private boolean multipleChoice = false;
    
    private Integer maxChoices = 1;
    
    private boolean anonymous = true;
}
