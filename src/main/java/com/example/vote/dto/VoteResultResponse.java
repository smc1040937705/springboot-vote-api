package com.example.vote.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VoteResultResponse {

    private Long id;
    private String title;
    private Boolean isAnonymous;
    private Boolean isMultiple;
    private LocalDateTime deadline;
    private Boolean isClosed;
    private Integer totalVotes;
    private List<OptionResult> options;

    @Data
    public static class OptionResult {
        private Long id;
        private String content;
        private Integer voteCount;
        private String percentage;
    }
}
