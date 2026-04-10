package com.example.vote.controller;

import com.example.vote.common.Result;
import com.example.vote.dto.CastVoteRequest;
import com.example.vote.dto.CreateVoteRequest;
import com.example.vote.dto.VoteResultResponse;
import com.example.vote.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping
    public Result<Long> createVote(@Valid @RequestBody CreateVoteRequest request) {
        Long voteId = voteService.createVote(request);
        return Result.success(voteId);
    }

    @PostMapping("/cast")
    public Result<Void> castVote(@Valid @RequestBody CastVoteRequest request) {
        voteService.castVote(request);
        return Result.success();
    }

    @GetMapping("/{id}/result")
    public Result<VoteResultResponse> getVoteResult(@PathVariable Long id) {
        VoteResultResponse result = voteService.getVoteResult(id);
        return Result.success(result);
    }
}
