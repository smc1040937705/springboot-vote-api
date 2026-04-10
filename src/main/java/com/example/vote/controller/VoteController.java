package com.example.vote.controller;

import com.example.vote.common.Result;
import com.example.vote.dto.VoteCreateRequest;
import com.example.vote.dto.VoteResponse;
import com.example.vote.dto.VoteResultResponse;
import com.example.vote.dto.VoteSubmitRequest;
import com.example.vote.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {
    
    private final VoteService voteService;
    
    @PostMapping
    public Result<VoteResponse> createVote(@Valid @RequestBody VoteCreateRequest request) {
        VoteResponse response = voteService.createVote(request);
        return Result.success(response);
    }
    
    @GetMapping("/{id}")
    public Result<VoteResponse> getVote(@PathVariable Long id) {
        VoteResponse response = voteService.getVote(id);
        return Result.success(response);
    }
    
    @GetMapping
    public Result<List<VoteResponse>> getAllVotes() {
        List<VoteResponse> responses = voteService.getAllVotes();
        return Result.success(responses);
    }
    
    @PostMapping("/submit")
    public Result<Void> submitVote(@Valid @RequestBody VoteSubmitRequest request) {
        voteService.submitVote(request);
        return Result.success();
    }
    
    @GetMapping("/{id}/result")
    public Result<VoteResultResponse> getVoteResult(@PathVariable Long id) {
        VoteResultResponse response = voteService.getVoteResult(id);
        return Result.success(response);
    }
}
