package com.example.vote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VoteCreateRequest {
    @NotBlank(message = "投票标题不能为空")
    private String title;
    
    @NotEmpty(message = "选项不能为空")
    private List<String> options;
    
    @NotNull(message = "截止时间不能为空")
    private LocalDateTime deadline;
    
    private Boolean anonymous = false;
    
    private Boolean multipleChoice = false;
    
    private Integer maxChoices = 1;
}
