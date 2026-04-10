package com.example.vote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateVoteRequest {

    @NotBlank(message = "投票标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    @NotNull(message = "是否匿名不能为空")
    private Boolean isAnonymous;

    @NotNull(message = "是否多选不能为空")
    private Boolean isMultiple;

    private Integer maxChoices;

    @NotNull(message = "截止时间不能为空")
    private LocalDateTime deadline;

    @NotNull(message = "选项列表不能为空")
    @Size(min = 2, message = "至少需要2个选项")
    private List<@NotBlank(message = "选项内容不能为空") String> options;
}
