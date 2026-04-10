package com.vote.controller;

import com.vote.common.ApiResponse;
import com.vote.dto.CastVoteRequest;
import com.vote.dto.CreateVoteRequest;
import com.vote.dto.VoteResponse;
import com.vote.dto.VoteResultResponse;
import com.vote.service.VoteService;
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
    public ApiResponse<VoteResponse> createVote(@Valid @RequestBody CreateVoteRequest request) {
        VoteResponse response = voteService.createVote(request);
        return ApiResponse.success("Vote created successfully", response);
    }
    
    @PostMapping("/{voteId}/cast")
    public ApiResponse<Void> castVote(
            @PathVariable Long voteId,
            @Valid @RequestBody CastVoteRequest request) {
        voteService.castVote(voteId, request);
        return ApiResponse.success("Vote cast successfully", null);
    }
    
    @GetMapping("/{voteId}/result")
    public ApiResponse<VoteResultResponse> getVoteResult(@PathVariable Long voteId) {
        VoteResultResponse response = voteService.getVoteResult(voteId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/{voteId}")
    public ApiResponse<VoteResponse> getVote(@PathVariable Long voteId) {
        VoteResponse response = voteService.getVote(voteId);
        return ApiResponse.success(response);
    }
    
    @GetMapping
    public ApiResponse<List<VoteResponse>> getAllVotes() {
        List<VoteResponse> responses = voteService.getAllVotes();
        return ApiResponse.success(responses);
    }
}
