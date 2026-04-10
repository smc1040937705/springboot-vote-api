package com.example.vote.task;

import com.example.vote.entity.Vote;
import com.example.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VoteExpireTask {

    private final VoteRepository voteRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void closeExpiredVotes() {
        List<Vote> expiredVotes = voteRepository.findExpiredVotes(LocalDateTime.now());
        for (Vote vote : expiredVotes) {
            vote.setIsClosed(true);
            voteRepository.save(vote);
        }
    }
}
