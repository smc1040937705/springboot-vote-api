package com.example.vote.dto;

import lombok.Data;

@Data
public class OptionResponse {
    private Long id;
    private String content;
    private Integer voteCount;
    private Double percentage;
}
