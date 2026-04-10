package com.example.vote.service;

import com.example.vote.dto.*;
import com.example.vote.entity.Vote;
import com.example.vote.entity.VoteOption;
import com.example.vote.entity.VoteRecord;
import com.example.vote.exception.BusinessException;
import com.example.vote.exception.ErrorCode;
import com.example.vote.repository.VoteOptionRepository;
import com.example.vote.repository.VoteRecordRepository;
import com.example.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteService {
    
    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteRecordRepository voteRecordRepository;
    
    @Transactional
    public VoteResponse createVote(VoteCreateRequest request) {
        Vote vote = new Vote();
        vote.setTitle(request.getTitle());
        vote.setDeadline(request.getDeadline());
        vote.setAnonymous(request.getAnonymous() != null ? request.getAnonymous() : false);
        vote.setMultipleChoice(request.getMultipleChoice() != null ? request.getMultipleChoice() : false);
        vote.setMaxChoices(vote.getMultipleChoice() ? (request.getMaxChoices() != null ? request.getMaxChoices() : 1) : 1);
        vote.setClosed(false);
        
        Vote savedVote = voteRepository.save(vote);
        
        for (String optionContent : request.getOptions()) {
            VoteOption option = new VoteOption();
            option.setContent(optionContent);
            option.setVoteCount(0);
            option.setVote(savedVote);
            voteOptionRepository.save(option);
        }
        
        return toVoteResponse(savedVote);
    }
    
    @Transactional(readOnly = true)
    public VoteResponse getVote(Long id) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOTE_NOT_FOUND));
        return toVoteResponse(vote);
    }
    
    @Transactional(readOnly = true)
    public List<VoteResponse> getAllVotes() {
        return voteRepository.findAll().stream()
                .map(this::toVoteResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void submitVote(VoteSubmitRequest request) {
        Vote vote = voteRepository.findById(request.getVoteId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VOTE_NOT_FOUND));
        
        if (vote.getClosed()) {
            throw new BusinessException(ErrorCode.VOTE_CLOSED);
        }
        
        if (LocalDateTime.now().isAfter(vote.getDeadline())) {
            throw new BusinessException(ErrorCode.VOTE_DEADLINE_PASSED);
        }
        
        if (voteRecordRepository.existsByVoteIdAndVoterId(request.getVoteId(), request.getVoterId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_VOTE);
        }
        
        List<VoteOption> options = voteOptionRepository.findByVoteId(request.getVoteId());
        List<Long> validOptionIds = options.stream()
                .map(VoteOption::getId)
                .collect(Collectors.toList());
        
        for (Long optionId : request.getOptionIds()) {
            if (!validOptionIds.contains(optionId)) {
                throw new BusinessException(ErrorCode.INVALID_OPTION);
            }
        }
        
        if (vote.getMultipleChoice()) {
            if (request.getOptionIds().size() > vote.getMaxChoices()) {
                throw new BusinessException(ErrorCode.EXCEED_MAX_CHOICES);
            }
        } else {
            if (request.getOptionIds().size() > 1) {
                throw new BusinessException(ErrorCode.SINGLE_CHOICE_ONLY);
            }
        }
        
        for (Long optionId : request.getOptionIds()) {
            VoteOption option = voteOptionRepository.findById(optionId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_OPTION));
            option.setVoteCount(option.getVoteCount() + 1);
            voteOptionRepository.save(option);
            
            VoteRecord record = new VoteRecord();
            record.setVoteId(request.getVoteId());
            record.setOptionId(optionId);
            record.setVoterId(request.getVoterId());
            record.setVoteTime(LocalDateTime.now());
            voteRecordRepository.save(record);
        }
    }
    
    @Transactional(readOnly = true)
    public VoteResultResponse getVoteResult(Long id) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOTE_NOT_FOUND));
        
        List<VoteOption> options = voteOptionRepository.findByVoteId(id);
        long totalVotes = voteRecordRepository.countByVoteId(id);
        
        VoteResultResponse response = new VoteResultResponse();
        response.setId(vote.getId());
        response.setTitle(vote.getTitle());
        response.setAnonymous(vote.getAnonymous());
        response.setClosed(vote.getClosed());
        response.setDeadline(vote.getDeadline());
        response.setTotalVotes(totalVotes);
        
        List<VoteResultResponse.OptionResult> optionResults = options.stream()
                .map(opt -> {
                    VoteResultResponse.OptionResult or = new VoteResultResponse.OptionResult();
                    or.setId(opt.getId());
                    or.setContent(opt.getContent());
                    or.setVoteCount(opt.getVoteCount());
                    or.setPercentage(totalVotes > 0 
                            ? Math.round((double) opt.getVoteCount() / totalVotes * 10000) / 100.0 
                            : 0.0);
                    return or;
                })
                .collect(Collectors.toList());
        
        response.setOptions(optionResults);
        return response;
    }
    
    @Transactional
    public void closeExpiredVotes() {
        List<Vote> expiredVotes = voteRepository.findByClosedFalseAndDeadlineBefore(LocalDateTime.now());
        for (Vote vote : expiredVotes) {
            vote.setClosed(true);
            voteRepository.save(vote);
        }
    }
    
    private VoteResponse toVoteResponse(Vote vote) {
        List<VoteOption> options = voteOptionRepository.findByVoteId(vote.getId());
        long totalVotes = voteRecordRepository.countByVoteId(vote.getId());
        
        VoteResponse response = new VoteResponse();
        response.setId(vote.getId());
        response.setTitle(vote.getTitle());
        response.setAnonymous(vote.getAnonymous());
        response.setMultipleChoice(vote.getMultipleChoice());
        response.setMaxChoices(vote.getMaxChoices());
        response.setDeadline(vote.getDeadline());
        response.setClosed(vote.getClosed());
        
        List<OptionResponse> optionResponses = options.stream()
                .map(opt -> {
                    OptionResponse or = new OptionResponse();
                    or.setId(opt.getId());
                    or.setContent(opt.getContent());
                    or.setVoteCount(opt.getVoteCount());
                    or.setPercentage(totalVotes > 0 
                            ? Math.round((double) opt.getVoteCount() / totalVotes * 10000) / 100.0 
                            : 0.0);
                    return or;
                })
                .collect(Collectors.toList());
        
        response.setOptions(optionResponses);
        return response;
    }
}
