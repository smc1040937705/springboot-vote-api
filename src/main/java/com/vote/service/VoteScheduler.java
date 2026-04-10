package com.vote.service;

import com.vote.entity.Vote;
import com.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteScheduler {
    
    private final VoteRepository voteRepository;
    
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void closeExpiredVotes() {
        List<Vote> expiredVotes = voteRepository.findAll().stream()
                .filter(vote -> !vote.isClosed() && vote.isExpired())
                .toList();
        
        for (Vote vote : expiredVotes) {
            vote.setClosed(true);
            voteRepository.save(vote);
            log.info("Vote {} has been automatically closed due to expiration", vote.getId());
        }
    }
}
