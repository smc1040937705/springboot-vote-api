package com.example.vote.service;

import com.example.vote.dto.VoteCreateRequest;
import com.example.vote.dto.VoteResponse;
import com.example.vote.dto.VoteResultResponse;
import com.example.vote.dto.VoteSubmitRequest;
import com.example.vote.exception.BusinessException;
import com.example.vote.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class VoteServiceTest {

    @Autowired
    private VoteService voteService;

    @Test
    void testDuplicateVote() {
        VoteCreateRequest createRequest = new VoteCreateRequest();
        createRequest.setTitle("测试投票");
        createRequest.setOptions(Arrays.asList("选项A", "选项B", "选项C"));
        createRequest.setDeadline(LocalDateTime.now().plusDays(1));
        createRequest.setAnonymous(false);
        createRequest.setMultipleChoice(false);

        VoteResponse vote = voteService.createVote(createRequest);

        VoteSubmitRequest submitRequest = new VoteSubmitRequest();
        submitRequest.setVoteId(vote.getId());
        submitRequest.setOptionIds(Collections.singletonList(vote.getOptions().get(0).getId()));
        submitRequest.setVoterId("user001");

        voteService.submitVote(submitRequest);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voteService.submitVote(submitRequest);
        });

        assertEquals(ErrorCode.DUPLICATE_VOTE.getCode(), exception.getCode());
    }

    @Test
    void testVoteStatisticsCalculation() {
        VoteCreateRequest createRequest = new VoteCreateRequest();
        createRequest.setTitle("统计测试投票");
        createRequest.setOptions(Arrays.asList("选项1", "选项2", "选项3"));
        createRequest.setDeadline(LocalDateTime.now().plusDays(1));
        createRequest.setAnonymous(false);
        createRequest.setMultipleChoice(false);

        VoteResponse vote = voteService.createVote(createRequest);
        List<Long> optionIds = Arrays.asList(
                vote.getOptions().get(0).getId(),
                vote.getOptions().get(1).getId(),
                vote.getOptions().get(2).getId()
        );

        VoteSubmitRequest submit1 = new VoteSubmitRequest();
        submit1.setVoteId(vote.getId());
        submit1.setOptionIds(Collections.singletonList(optionIds.get(0)));
        submit1.setVoterId("user1");
        voteService.submitVote(submit1);

        VoteSubmitRequest submit2 = new VoteSubmitRequest();
        submit2.setVoteId(vote.getId());
        submit2.setOptionIds(Collections.singletonList(optionIds.get(0)));
        submit2.setVoterId("user2");
        voteService.submitVote(submit2);

        VoteSubmitRequest submit3 = new VoteSubmitRequest();
        submit3.setVoteId(vote.getId());
        submit3.setOptionIds(Collections.singletonList(optionIds.get(1)));
        submit3.setVoterId("user3");
        voteService.submitVote(submit3);

        VoteResultResponse result = voteService.getVoteResult(vote.getId());

        assertEquals(3L, result.getTotalVotes());

        VoteResultResponse.OptionResult option0 = result.getOptions().get(0);
        assertEquals(2, option0.getVoteCount());
        assertEquals(66.67, option0.getPercentage(), 0.01);

        VoteResultResponse.OptionResult option1 = result.getOptions().get(1);
        assertEquals(1, option1.getVoteCount());
        assertEquals(33.33, option1.getPercentage(), 0.01);

        VoteResultResponse.OptionResult option2 = result.getOptions().get(2);
        assertEquals(0, option2.getVoteCount());
        assertEquals(0.0, option2.getPercentage(), 0.01);
    }

    @Test
    void testVoteAfterDeadline() {
        VoteCreateRequest createRequest = new VoteCreateRequest();
        createRequest.setTitle("截止测试投票");
        createRequest.setOptions(Arrays.asList("选项A", "选项B"));
        createRequest.setDeadline(LocalDateTime.now().minusHours(1));
        createRequest.setAnonymous(false);
        createRequest.setMultipleChoice(false);

        VoteResponse vote = voteService.createVote(createRequest);

        VoteSubmitRequest submitRequest = new VoteSubmitRequest();
        submitRequest.setVoteId(vote.getId());
        submitRequest.setOptionIds(Collections.singletonList(vote.getOptions().get(0).getId()));
        submitRequest.setVoterId("user001");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voteService.submitVote(submitRequest);
        });

        assertEquals(ErrorCode.VOTE_DEADLINE_PASSED.getCode(), exception.getCode());
    }

    @Test
    void testMultipleChoiceLimit() {
        VoteCreateRequest createRequest = new VoteCreateRequest();
        createRequest.setTitle("多选测试投票");
        createRequest.setOptions(Arrays.asList("选项1", "选项2", "选项3", "选项4"));
        createRequest.setDeadline(LocalDateTime.now().plusDays(1));
        createRequest.setAnonymous(false);
        createRequest.setMultipleChoice(true);
        createRequest.setMaxChoices(2);

        VoteResponse vote = voteService.createVote(createRequest);

        VoteSubmitRequest validRequest = new VoteSubmitRequest();
        validRequest.setVoteId(vote.getId());
        validRequest.setOptionIds(Arrays.asList(
                vote.getOptions().get(0).getId(),
                vote.getOptions().get(1).getId()
        ));
        validRequest.setVoterId("user001");

        assertDoesNotThrow(() -> voteService.submitVote(validRequest));

        VoteSubmitRequest exceedRequest = new VoteSubmitRequest();
        exceedRequest.setVoteId(vote.getId());
        exceedRequest.setOptionIds(Arrays.asList(
                vote.getOptions().get(0).getId(),
                vote.getOptions().get(1).getId(),
                vote.getOptions().get(2).getId()
        ));
        exceedRequest.setVoterId("user002");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voteService.submitVote(exceedRequest);
        });

        assertEquals(ErrorCode.EXCEED_MAX_CHOICES.getCode(), exception.getCode());
    }

    @Test
    void testSingleChoiceRestriction() {
        VoteCreateRequest createRequest = new VoteCreateRequest();
        createRequest.setTitle("单选测试投票");
        createRequest.setOptions(Arrays.asList("选项1", "选项2", "选项3"));
        createRequest.setDeadline(LocalDateTime.now().plusDays(1));
        createRequest.setAnonymous(false);
        createRequest.setMultipleChoice(false);

        VoteResponse vote = voteService.createVote(createRequest);

        VoteSubmitRequest submitRequest = new VoteSubmitRequest();
        submitRequest.setVoteId(vote.getId());
        submitRequest.setOptionIds(Arrays.asList(
                vote.getOptions().get(0).getId(),
                vote.getOptions().get(1).getId()
        ));
        submitRequest.setVoterId("user001");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voteService.submitVote(submitRequest);
        });

        assertEquals(ErrorCode.SINGLE_CHOICE_ONLY.getCode(), exception.getCode());
    }

    @Test
    void testVoteClosed() {
        VoteCreateRequest createRequest = new VoteCreateRequest();
        createRequest.setTitle("关闭测试投票");
        createRequest.setOptions(Arrays.asList("选项A", "选项B"));
        createRequest.setDeadline(LocalDateTime.now().plusDays(1));
        createRequest.setAnonymous(false);
        createRequest.setMultipleChoice(false);

        VoteResponse vote = voteService.createVote(createRequest);

        voteService.closeExpiredVotes();

        VoteSubmitRequest submitRequest = new VoteSubmitRequest();
        submitRequest.setVoteId(vote.getId());
        submitRequest.setOptionIds(Collections.singletonList(vote.getOptions().get(0).getId()));
        submitRequest.setVoterId("user001");

        assertDoesNotThrow(() -> voteService.submitVote(submitRequest));
    }

    @Test
    void testAnonymousVote() {
        VoteCreateRequest createRequest = new VoteCreateRequest();
        createRequest.setTitle("匿名测试投票");
        createRequest.setOptions(Arrays.asList("选项A", "选项B"));
        createRequest.setDeadline(LocalDateTime.now().plusDays(1));
        createRequest.setAnonymous(true);
        createRequest.setMultipleChoice(false);

        VoteResponse vote = voteService.createVote(createRequest);

        assertTrue(vote.getAnonymous());

        VoteSubmitRequest submitRequest = new VoteSubmitRequest();
        submitRequest.setVoteId(vote.getId());
        submitRequest.setOptionIds(Collections.singletonList(vote.getOptions().get(0).getId()));
        submitRequest.setVoterId("anonymous_user");

        assertDoesNotThrow(() -> voteService.submitVote(submitRequest));
    }
}
