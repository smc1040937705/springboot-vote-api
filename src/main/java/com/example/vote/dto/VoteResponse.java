package com.example.vote.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VoteResponse {
    private Long id;
    private String title;
    private Boolean anonymous;
    private Boolean multipleChoice;
    private Integer maxChoices;
    private LocalDateTime deadline;
    private Boolean closed;
    private List<OptionResponse> options;
}
