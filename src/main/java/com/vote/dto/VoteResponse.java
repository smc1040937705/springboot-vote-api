package com.vote.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VoteResponse {
    private Long id;
    private String title;
    private LocalDateTime deadline;
    private boolean multipleChoice;
    private Integer maxChoices;
    private boolean anonymous;
    private boolean closed;
    private List<OptionResponse> options;
    
    @Data
    public static class OptionResponse {
        private Long id;
        private String content;
    }
}
