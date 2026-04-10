package com.vote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.dto.CastVoteRequest;
import com.vote.dto.CreateVoteRequest;
import com.vote.repository.VoteRecordRepository;
import com.vote.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VoteApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private VoteRecordRepository voteRecordRepository;

    @BeforeEach
    void setUp() {
        voteRecordRepository.deleteAll();
        voteRepository.deleteAll();
    }

    @Test
    @DisplayName("创建投票成功")
    void createVoteSuccess() throws Exception {
        CreateVoteRequest request = new CreateVoteRequest();
        request.setTitle("Test Vote");
        request.setOptions(Arrays.asList("Option A", "Option B", "Option C"));
        request.setDeadline(LocalDateTime.now().plusDays(1));
        request.setMultipleChoice(false);
        request.setMaxChoices(1);
        request.setAnonymous(true);

        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Test Vote"))
                .andExpect(jsonPath("$.data.options", hasSize(3)));
    }

    @Test
    @DisplayName("重复投票拦截测试")
    void duplicateVotePreventionTest() throws Exception {
        VoteInfo voteInfo = createTestVoteWithOptionIds(false, 1);

        CastVoteRequest firstVote = new CastVoteRequest();
        firstVote.setUserId("user1");
        firstVote.setOptionIds(List.of(voteInfo.optionIds.get(0)));

        mockMvc.perform(post("/api/votes/" + voteInfo.voteId + "/cast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstVote)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        CastVoteRequest secondVote = new CastVoteRequest();
        secondVote.setUserId("user1");
        secondVote.setOptionIds(List.of(voteInfo.optionIds.get(1)));

        mockMvc.perform(post("/api/votes/" + voteInfo.voteId + "/cast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondVote)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User has already voted"));
    }

    @Test
    @DisplayName("投票统计计算正确性测试")
    void voteStatisticsCorrectnessTest() throws Exception {
        VoteInfo voteInfo = createTestVoteWithOptionIds(false, 1);

        castVote(voteInfo.voteId, "user1", voteInfo.optionIds.get(0));
        castVote(voteInfo.voteId, "user2", voteInfo.optionIds.get(0));
        castVote(voteInfo.voteId, "user3", voteInfo.optionIds.get(1));
        castVote(voteInfo.voteId, "user4", voteInfo.optionIds.get(1));
        castVote(voteInfo.voteId, "user5", voteInfo.optionIds.get(2));

        MvcResult result = mockMvc.perform(get("/api/votes/" + voteInfo.voteId + "/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalVotes").value(5))
                .andExpect(jsonPath("$.data.options", hasSize(3)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        
        mockMvc.perform(get("/api/votes/" + voteInfo.voteId + "/result"))
                .andExpect(jsonPath("$.data.options[0].voteCount").value(2))
                .andExpect(jsonPath("$.data.options[0].percentage").value(40.00))
                .andExpect(jsonPath("$.data.options[1].voteCount").value(2))
                .andExpect(jsonPath("$.data.options[1].percentage").value(40.00))
                .andExpect(jsonPath("$.data.options[2].voteCount").value(1))
                .andExpect(jsonPath("$.data.options[2].percentage").value(20.00));
    }

    @Test
    @DisplayName("截止时间后禁止投票测试")
    void voteAfterDeadlinePreventionTest() throws Exception {
        CreateVoteRequest request = new CreateVoteRequest();
        request.setTitle("Expired Vote");
        request.setOptions(Arrays.asList("Option A", "Option B"));
        request.setDeadline(LocalDateTime.now().plusSeconds(1));
        request.setMultipleChoice(false);
        request.setMaxChoices(1);

        MvcResult createResult = mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        Long voteId = objectMapper.readTree(response).path("data").path("id").asLong();
        Long firstOptionId = objectMapper.readTree(response).path("data").path("options").get(0).path("id").asLong();

        Thread.sleep(1500);

        CastVoteRequest voteRequest = new CastVoteRequest();
        voteRequest.setUserId("user1");
        voteRequest.setOptionIds(List.of(firstOptionId));

        mockMvc.perform(post("/api/votes/" + voteId + "/cast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Vote is closed or expired"));
    }

    @Test
    @DisplayName("多选数量限制验证测试")
    void multipleChoiceLimitValidationTest() throws Exception {
        VoteInfo voteInfo = createTestVoteWithOptionIds(true, 2);

        CastVoteRequest validVote = new CastVoteRequest();
        validVote.setUserId("user1");
        validVote.setOptionIds(Arrays.asList(voteInfo.optionIds.get(0), voteInfo.optionIds.get(1)));

        mockMvc.perform(post("/api/votes/" + voteInfo.voteId + "/cast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validVote)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        CastVoteRequest invalidVote = new CastVoteRequest();
        invalidVote.setUserId("user2");
        invalidVote.setOptionIds(Arrays.asList(voteInfo.optionIds.get(0), voteInfo.optionIds.get(1), voteInfo.optionIds.get(2)));

        mockMvc.perform(post("/api/votes/" + voteInfo.voteId + "/cast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidVote)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Exceeded maximum choices allowed: 2"));
    }

    @Test
    @DisplayName("单选投票选择多个选项时拒绝")
    void singleChoiceMultipleOptionsRejectionTest() throws Exception {
        VoteInfo voteInfo = createTestVoteWithOptionIds(false, 1);

        CastVoteRequest invalidVote = new CastVoteRequest();
        invalidVote.setUserId("user1");
        invalidVote.setOptionIds(Arrays.asList(voteInfo.optionIds.get(0), voteInfo.optionIds.get(1)));

        mockMvc.perform(post("/api/votes/" + voteInfo.voteId + "/cast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidVote)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This vote allows only one choice"));
    }

    @Test
    @DisplayName("创建投票时选项数量不足")
    void createVoteWithInsufficientOptionsTest() throws Exception {
        CreateVoteRequest request = new CreateVoteRequest();
        request.setTitle("Invalid Vote");
        request.setOptions(List.of("Only One Option"));
        request.setDeadline(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("At least 2 options are required"));
    }

    @Test
    @DisplayName("创建投票时截止时间在过去")
    void createVoteWithPastDeadlineTest() throws Exception {
        CreateVoteRequest request = new CreateVoteRequest();
        request.setTitle("Invalid Vote");
        request.setOptions(Arrays.asList("Option A", "Option B"));
        request.setDeadline(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Deadline must be in the future"));
    }

    @Test
    @DisplayName("查询不存在的投票")
    void getNonExistentVoteTest() throws Exception {
        mockMvc.perform(get("/api/votes/99999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Vote not found"));
    }

    @Test
    @DisplayName("投票给不存在的选项")
    void voteForNonExistentOptionTest() throws Exception {
        VoteInfo voteInfo = createTestVoteWithOptionIds(false, 1);

        CastVoteRequest voteRequest = new CastVoteRequest();
        voteRequest.setUserId("user1");
        voteRequest.setOptionIds(List.of(99999L));

        mockMvc.perform(post("/api/votes/" + voteInfo.voteId + "/cast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid option ID"));
    }

    private static class VoteInfo {
        Long voteId;
        List<Long> optionIds;
        
        VoteInfo(Long voteId, List<Long> optionIds) {
            this.voteId = voteId;
            this.optionIds = optionIds;
        }
    }

    private VoteInfo createTestVoteWithOptionIds(boolean multipleChoice, int maxChoices) throws Exception {
        CreateVoteRequest request = new CreateVoteRequest();
        request.setTitle("Test Vote");
        request.setOptions(Arrays.asList("Option A", "Option B", "Option C"));
        request.setDeadline(LocalDateTime.now().plusDays(1));
        request.setMultipleChoice(multipleChoice);
        request.setMaxChoices(maxChoices);

        MvcResult result = mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long voteId = objectMapper.readTree(response).path("data").path("id").asLong();
        
        List<Long> optionIds = new ArrayList<>();
        var optionsArray = objectMapper.readTree(response).path("data").path("options");
        for (int i = 0; i < optionsArray.size(); i++) {
            optionIds.add(optionsArray.get(i).path("id").asLong());
        }
        
        return new VoteInfo(voteId, optionIds);
    }

    private Long createTestVote(boolean multipleChoice, int maxChoices) throws Exception {
        return createTestVoteWithOptionIds(multipleChoice, maxChoices).voteId;
    }

    private void castVote(Long voteId, String userId, Long optionId) throws Exception {
        CastVoteRequest voteRequest = new CastVoteRequest();
        voteRequest.setUserId(userId);
        voteRequest.setOptionIds(List.of(optionId));

        mockMvc.perform(post("/api/votes/" + voteId + "/cast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isOk());
    }
}
