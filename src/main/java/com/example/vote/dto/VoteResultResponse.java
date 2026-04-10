package com.example.vote.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VoteResultResponse {
    private Long id;
    private String title;
    private Boolean anonymous;
    private Boolean closed;
    private LocalDateTime deadline;
    private Long totalVotes;
    private List<OptionResult> options;
    
    @Data
    public static class OptionResult {
        private Long id;
        private String content;
        private Integer voteCount;
        private Double percentage;
    }
}
