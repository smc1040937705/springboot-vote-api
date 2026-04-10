package com.vote.service;

import com.vote.common.BusinessException;
import com.vote.dto.CastVoteRequest;
import com.vote.dto.CreateVoteRequest;
import com.vote.dto.VoteResponse;
import com.vote.dto.VoteResultResponse;
import com.vote.entity.Vote;
import com.vote.entity.VoteOption;
import com.vote.entity.VoteRecord;
import com.vote.repository.VoteOptionRepository;
import com.vote.repository.VoteRecordRepository;
import com.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VoteService {
    
    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteRecordRepository voteRecordRepository;
    
    @Transactional
    public VoteResponse createVote(CreateVoteRequest request) {
        if (request.getOptions().size() < 2) {
            throw new BusinessException("At least 2 options are required");
        }
        
        if (request.getDeadline().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("Deadline must be in the future");
        }
        
        if (request.isMultipleChoice() && request.getMaxChoices() < 1) {
            throw new BusinessException("Max choices must be at least 1 for multiple choice vote");
        }
        
        if (request.isMultipleChoice() && request.getMaxChoices() > request.getOptions().size()) {
            throw new BusinessException("Max choices cannot exceed number of options");
        }
        
        Vote vote = new Vote();
        vote.setTitle(request.getTitle());
        vote.setDeadline(request.getDeadline());
        vote.setMultipleChoice(request.isMultipleChoice());
        vote.setMaxChoices(request.getMaxChoices());
        vote.setAnonymous(request.isAnonymous());
        
        List<VoteOption> options = new ArrayList<>();
        for (String content : request.getOptions()) {
            VoteOption option = new VoteOption();
            option.setContent(content);
            option.setVote(vote);
            options.add(option);
        }
        vote.setOptions(options);
        
        Vote savedVote = voteRepository.save(vote);
        return toVoteResponse(savedVote);
    }
    
    @Transactional
    public void castVote(Long voteId, CastVoteRequest request) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new BusinessException("Vote not found"));
        
        if (vote.isClosed() || vote.isExpired()) {
            throw new BusinessException("Vote is closed or expired");
        }
        
        if (voteRecordRepository.existsByVoteIdAndUserId(voteId, request.getUserId())) {
            throw new BusinessException("User has already voted");
        }
        
        List<Long> optionIds = request.getOptionIds();
        Set<Long> uniqueOptionIds = new HashSet<>(optionIds);
        
        if (uniqueOptionIds.size() != optionIds.size()) {
            throw new BusinessException("Duplicate options are not allowed");
        }
        
        if (!vote.isMultipleChoice() && optionIds.size() > 1) {
            throw new BusinessException("This vote allows only one choice");
        }
        
        if (vote.isMultipleChoice() && optionIds.size() > vote.getMaxChoices()) {
            throw new BusinessException("Exceeded maximum choices allowed: " + vote.getMaxChoices());
        }
        
        List<VoteOption> selectedOptions = voteOptionRepository.findAllById(optionIds);
        if (selectedOptions.size() != optionIds.size()) {
            throw new BusinessException("Invalid option ID");
        }
        
        for (VoteOption option : selectedOptions) {
            if (!option.getVote().getId().equals(voteId)) {
                throw new BusinessException("Option does not belong to this vote");
            }
        }
        
        for (Long optionId : optionIds) {
            VoteOption option = selectedOptions.stream()
                    .filter(o -> o.getId().equals(optionId))
                    .findFirst()
                    .orElseThrow();
            
            VoteRecord record = new VoteRecord();
            record.setVote(vote);
            record.setOption(option);
            record.setUserId(request.getUserId());
            voteRecordRepository.save(record);
        }
    }
    
    @Transactional(readOnly = true)
    public VoteResultResponse getVoteResult(Long voteId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new BusinessException("Vote not found"));
        
        if (vote.isExpired() && !vote.isClosed()) {
            vote.setClosed(true);
            voteRepository.save(vote);
        }
        
        List<VoteRecord> allRecords = voteRecordRepository.findByVoteId(voteId);
        long totalVotes = allRecords.stream()
                .map(VoteRecord::getUserId)
                .distinct()
                .count();
        
        VoteResultResponse response = new VoteResultResponse();
        response.setVoteId(vote.getId());
        response.setTitle(vote.getTitle());
        response.setClosed(vote.isClosed() || vote.isExpired());
        response.setTotalVotes(totalVotes);
        
        List<VoteResultResponse.OptionResult> optionResults = new ArrayList<>();
        for (VoteOption option : vote.getOptions()) {
            VoteResultResponse.OptionResult result = new VoteResultResponse.OptionResult();
            result.setOptionId(option.getId());
            result.setContent(option.getContent());
            
            long voteCount = voteRecordRepository.countByOptionId(option.getId());
            result.setVoteCount(voteCount);
            
            BigDecimal percentage;
            if (totalVotes > 0) {
                percentage = BigDecimal.valueOf(voteCount * 100.0 / vote.getOptions().stream()
                        .mapToLong(o -> voteRecordRepository.countByOptionId(o.getId()))
                        .sum())
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                percentage = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
            result.setPercentage(percentage);
            
            optionResults.add(result);
        }
        response.setOptions(optionResults);
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public VoteResponse getVote(Long voteId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new BusinessException("Vote not found"));
        return toVoteResponse(vote);
    }
    
    @Transactional(readOnly = true)
    public List<VoteResponse> getAllVotes() {
        return voteRepository.findAll().stream()
                .map(this::toVoteResponse)
                .toList();
    }
    
    private VoteResponse toVoteResponse(Vote vote) {
        VoteResponse response = new VoteResponse();
        response.setId(vote.getId());
        response.setTitle(vote.getTitle());
        response.setDeadline(vote.getDeadline());
        response.setMultipleChoice(vote.isMultipleChoice());
        response.setMaxChoices(vote.getMaxChoices());
        response.setAnonymous(vote.isAnonymous());
        response.setClosed(vote.isClosed() || vote.isExpired());
        
        List<VoteResponse.OptionResponse> optionResponses = vote.getOptions().stream()
                .map(option -> {
                    VoteResponse.OptionResponse optResponse = new VoteResponse.OptionResponse();
                    optResponse.setId(option.getId());
                    optResponse.setContent(option.getContent());
                    return optResponse;
                })
                .toList();
        response.setOptions(optionResponses);
        
        return response;
    }
}
