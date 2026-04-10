package com.vote.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VoteResultResponse {
    private Long voteId;
    private String title;
    private boolean closed;
    private long totalVotes;
    private List<OptionResult> options;
    
    @Data
    public static class OptionResult {
        private Long optionId;
        private String content;
        private long voteCount;
        private BigDecimal percentage;
    }
}
