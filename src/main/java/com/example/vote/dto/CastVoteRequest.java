package com.example.vote.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CastVoteRequest {

    @NotNull(message = "投票ID不能为空")
    private Long voteId;

    @NotEmpty(message = "至少选择一个选项")
    private List<Long> optionIds;

    private String userId;
}
