package com.example.vote;

import com.example.vote.dto.CastVoteRequest;
import com.example.vote.dto.CreateVoteRequest;
import com.example.vote.dto.VoteResultResponse;
import com.example.vote.entity.Vote;
import com.example.vote.exception.BusinessException;
import com.example.vote.repository.VoteRepository;
import com.example.vote.service.VoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class VoteServiceTest {

    @Autowired
    private VoteService voteService;

    @Autowired
    private VoteRepository voteRepository;

    @Test
    void testDuplicateVotePrevention() {
        CreateVoteRequest createRequest = new CreateVoteRequest();
        createRequest.setTitle("测试单选投票");
        createRequest.setIsAnonymous(true);
        createRequest.setIsMultiple(false);
        createRequest.setDeadline(LocalDateTime.now().plusDays(1));
        createRequest.setOptions(List.of("选项A", "选项B", "选项C"));
        Long voteId = voteService.createVote(createRequest);

        VoteResultResponse result = voteService.getVoteResult(voteId);
        Long optionAId = result.getOptions().get(0).getId();

        CastVoteRequest voteRequest = new CastVoteRequest();
        voteRequest.setVoteId(voteId);
        voteRequest.setOptionIds(List.of(optionAId));
        voteRequest.setUserId("user001");
        voteService.castVote(voteRequest);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voteService.castVote(voteRequest);
        });
        assertEquals("您已投过票了", exception.getMessage());
    }

    @Test
    void testVoteStatisticsCalculation() {
        CreateVoteRequest createRequest = new CreateVoteRequest();
        createRequest.setTitle("测试统计计算");
        createRequest.setIsAnonymous(true);
        createRequest.setIsMultiple(false);
        createRequest.setDeadline(LocalDateTime.now().plusDays(1));
        createRequest.setOptions(List.of("选项A", "选项B"));
        Long voteId = voteService.createVote(createRequest);

        VoteResultResponse result = voteService.getVoteResult(voteId);
        Long optionAId = result.getOptions().get(0).getId();
        Long optionBId = result.getOptions().get(1).getId();

        for (int i = 1; i <= 3; i++) {
            CastVoteRequest voteRequest = new CastVoteRequest();
            voteRequest.setVoteId(voteId);
            voteRequest.setOptionIds(List.of(optionAId));
            voteRequest.setUserId("user" + i);
            voteService.castVote(voteRequest);
        }

        for (int i = 4; i <= 5; i++) {
            CastVoteRequest voteRequest = new CastVoteRequest();
            voteRequest.setVoteId(voteId);
            voteRequest.setOptionIds(List.of(optionBId));
            voteRequest.setUserId("user" + i);
            voteService.castVote(voteRequest);
        }

        VoteResultResponse finalResult = voteService.getVoteResult(voteId);

        assertEquals(5, finalResult.getTotalVotes());
        assertEquals(3, finalResult.getOptions().get(0).getVoteCount());
        assertEquals(2, finalResult.getOptions().get(1).getVoteCount());
        assertEquals("60.00%", finalResult.getOptions().get(0).getPercentage());
        assertEquals("40.00%", finalResult.getOptions().get(1).getPercentage());
    }

    @Test
    void testVoteAfterDeadlineForbidden() {
        CreateVoteRequest createRequest = new CreateVoteRequest();
        createRequest.setTitle("测试截止投票");
        createRequest.setIsAnonymous(true);
        createRequest.setIsMultiple(false);
        createRequest.setDeadline(LocalDateTime.now().plusSeconds(1));
        createRequest.setOptions(List.of("选项A", "选项B"));
        Long voteId = voteService.createVote(createRequest);

        Vote vote = voteRepository.findById(voteId).orElseThrow();
        vote.setDeadline(LocalDateTime.now().minusMinutes(1));
        voteRepository.save(vote);

        VoteResultResponse result = voteService.getVoteResult(voteId);
        Long optionAId = result.getOptions().get(0).getId();

        CastVoteRequest voteRequest = new CastVoteRequest();
        voteRequest.setVoteId(voteId);
        voteRequest.setOptionIds(List.of(optionAId));
        voteRequest.setUserId("user001");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voteService.castVote(voteRequest);
        });
        assertEquals("投票已截止", exception.getMessage());
    }

    @Test
    void testClosedVoteForbidden() {
        CreateVoteRequest createRequest = new CreateVoteRequest();
        createRequest.setTitle("测试已关闭投票");
        createRequest.setIsAnonymous(true);
        createRequest.setIsMultiple(false);
        createRequest.setDeadline(LocalDateTime.now().plusDays(1));
        createRequest.setOptions(List.of("选项A", "选项B"));
        Long voteId = voteService.createVote(createRequest);

        Vote vote = voteRepository.findById(voteId).orElseThrow();
        vote.setIsClosed(true);
        voteRepository.save(vote);

        VoteResultResponse result = voteService.getVoteResult(voteId);
        Long optionAId = result.getOptions().get(0).getId();

        CastVoteRequest voteRequest = new CastVoteRequest();
        voteRequest.setVoteId(voteId);
        voteRequest.setOptionIds(List.of(optionAId));
        voteRequest.setUserId("user001");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voteService.castVote(voteRequest);
        });
        assertEquals("投票已关闭", exception.getMessage());
    }

    @Test
    void testMultipleChoiceLimit() {
        CreateVoteRequest createRequest = new CreateVoteRequest();
        createRequest.setTitle("测试多选限制");
        createRequest.setIsAnonymous(true);
        createRequest.setIsMultiple(true);
        createRequest.setMaxChoices(2);
        createRequest.setDeadline(LocalDateTime.now().plusDays(1));
        createRequest.setOptions(List.of("选项A", "选项B", "选项C", "选项D"));
        Long voteId = voteService.createVote(createRequest);

        VoteResultResponse result = voteService.getVoteResult(voteId);
        Long optionAId = result.getOptions().get(0).getId();
        Long optionBId = result.getOptions().get(1).getId();
        Long optionCId = result.getOptions().get(2).getId();

        CastVoteRequest validRequest = new CastVoteRequest();
        validRequest.setVoteId(voteId);
        validRequest.setOptionIds(List.of(optionAId, optionBId));
        validRequest.setUserId("user001");
        assertDoesNotThrow(() -> voteService.castVote(validRequest));

        CastVoteRequest exceedRequest = new CastVoteRequest();
        exceedRequest.setVoteId(voteId);
        exceedRequest.setOptionIds(List.of(optionAId, optionBId, optionCId));
        exceedRequest.setUserId("user002");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voteService.castVote(exceedRequest);
        });
        assertEquals("最多只能选择2个选项", exception.getMessage());
    }

    @Test
    void testSingleVoteMultipleSelectionForbidden() {
        CreateVoteRequest createRequest = new CreateVoteRequest();
        createRequest.setTitle("测试单选不能多选");
        createRequest.setIsAnonymous(true);
        createRequest.setIsMultiple(false);
        createRequest.setDeadline(LocalDateTime.now().plusDays(1));
        createRequest.setOptions(List.of("选项A", "选项B", "选项C"));
        Long voteId = voteService.createVote(createRequest);

        VoteResultResponse result = voteService.getVoteResult(voteId);
        Long optionAId = result.getOptions().get(0).getId();
        Long optionBId = result.getOptions().get(1).getId();

        CastVoteRequest voteRequest = new CastVoteRequest();
        voteRequest.setVoteId(voteId);
        voteRequest.setOptionIds(List.of(optionAId, optionBId));
        voteRequest.setUserId("user001");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voteService.castVote(voteRequest);
        });
        assertEquals("该投票为单选，只能选择一个选项", exception.getMessage());
    }

    @Test
    void testZeroVotePercentage() {
        CreateVoteRequest createRequest = new CreateVoteRequest();
        createRequest.setTitle("测试零投票占比");
        createRequest.setIsAnonymous(true);
        createRequest.setIsMultiple(false);
        createRequest.setDeadline(LocalDateTime.now().plusDays(1));
        createRequest.setOptions(List.of("选项A", "选项B"));
        Long voteId = voteService.createVote(createRequest);

        VoteResultResponse result = voteService.getVoteResult(voteId);

        assertEquals(0, result.getTotalVotes());
        assertEquals("0.00%", result.getOptions().get(0).getPercentage());
        assertEquals("0.00%", result.getOptions().get(1).getPercentage());
    }
}
