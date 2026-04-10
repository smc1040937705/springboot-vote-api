package com.vote.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CastVoteRequest {
    @NotNull(message = "User ID is required")
    private String userId;
    
    @NotEmpty(message = "At least one option must be selected")
    private List<@NotNull(message = "Option ID cannot be null") Long> optionIds;
}
