package com.example.vote.service;

import com.example.vote.dto.CastVoteRequest;
import com.example.vote.dto.CreateVoteRequest;
import com.example.vote.dto.VoteResultResponse;
import com.example.vote.entity.Vote;
import com.example.vote.entity.VoteOption;
import com.example.vote.entity.VoteRecord;
import com.example.vote.exception.BusinessException;
import com.example.vote.repository.VoteOptionRepository;
import com.example.vote.repository.VoteRecordRepository;
import com.example.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteRecordRepository voteRecordRepository;

    @Transactional
    public Long createVote(CreateVoteRequest request) {
        if (request.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException("截止时间不能早于当前时间");
        }

        Vote vote = new Vote();
        vote.setTitle(request.getTitle());
        vote.setIsAnonymous(request.getIsAnonymous());
        vote.setIsMultiple(request.getIsMultiple());
        vote.setMaxChoices(request.getMaxChoices() != null ? request.getMaxChoices() : request.getOptions().size());
        vote.setDeadline(request.getDeadline());
        vote.setIsClosed(false);

        List<VoteOption> options = request.getOptions().stream()
                .map(content -> {
                    VoteOption option = new VoteOption();
                    option.setContent(content);
                    option.setVote(vote);
                    return option;
                })
                .collect(Collectors.toList());

        vote.setOptions(options);
        Vote saved = voteRepository.save(vote);
        return saved.getId();
    }

    @Transactional
    public void castVote(CastVoteRequest request) {
        Vote vote = voteRepository.findById(request.getVoteId())
                .orElseThrow(() -> new BusinessException("投票不存在"));

        if (vote.getIsClosed()) {
            throw new BusinessException("投票已关闭");
        }

        if (vote.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException("投票已截止");
        }

        String userId = request.getUserId() != null ? request.getUserId() : UUID.randomUUID().toString();

        if (voteRecordRepository.existsByVoteIdAndUserId(vote.getId(), userId)) {
            throw new BusinessException("您已投过票了");
        }

        if (!vote.getIsMultiple() && request.getOptionIds().size() > 1) {
            throw new BusinessException("该投票为单选，只能选择一个选项");
        }

        if (vote.getIsMultiple() && request.getOptionIds().size() > vote.getMaxChoices()) {
            throw new BusinessException("最多只能选择" + vote.getMaxChoices() + "个选项");
        }

        List<VoteOption> voteOptions = voteOptionRepository.findByVoteId(vote.getId());
        Set<Long> validOptionIds = voteOptions.stream()
                .map(VoteOption::getId)
                .collect(Collectors.toSet());

        for (Long optionId : request.getOptionIds()) {
            if (!validOptionIds.contains(optionId)) {
                throw new BusinessException("选项ID " + optionId + " 无效");
            }
        }

        for (Long optionId : request.getOptionIds()) {
            VoteOption option = voteOptionRepository.findById(optionId).orElseThrow();
            option.setVoteCount(option.getVoteCount() + 1);
            voteOptionRepository.save(option);

            VoteRecord record = new VoteRecord();
            record.setVoteId(vote.getId());
            record.setOptionId(optionId);
            record.setUserId(userId);
            voteRecordRepository.save(record);
        }
    }

    @Transactional(readOnly = true)
    public VoteResultResponse getVoteResult(Long voteId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new BusinessException("投票不存在"));

        List<VoteOption> options = voteOptionRepository.findByVoteId(voteId);
        int totalVotes = options.stream()
                .mapToInt(VoteOption::getVoteCount)
                .sum();

        List<VoteResultResponse.OptionResult> optionResults = options.stream()
                .map(option -> {
                    VoteResultResponse.OptionResult result = new VoteResultResponse.OptionResult();
                    result.setId(option.getId());
                    result.setContent(option.getContent());
                    result.setVoteCount(option.getVoteCount());

                    if (totalVotes > 0) {
                        BigDecimal percentage = BigDecimal.valueOf(option.getVoteCount())
                                .divide(BigDecimal.valueOf(totalVotes), 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
                        result.setPercentage(percentage + "%");
                    } else {
                        result.setPercentage("0.00%");
                    }
                    return result;
                })
                .toList();

        VoteResultResponse response = new VoteResultResponse();
        response.setId(vote.getId());
        response.setTitle(vote.getTitle());
        response.setIsAnonymous(vote.getIsAnonymous());
        response.setIsMultiple(vote.getIsMultiple());
        response.setDeadline(vote.getDeadline());
        response.setIsClosed(vote.getIsClosed());
        response.setTotalVotes(totalVotes);
        response.setOptions(optionResults);

        return response;
    }
}
