package com.example.vote.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class VoteSubmitRequest {
    @NotNull(message = "投票ID不能为空")
    private Long voteId;
    
    @NotEmpty(message = "选项不能为空")
    private List<Long> optionIds;
    
    @NotNull(message = "投票人ID不能为空")
    private String voterId;
}
