package com.example.vote.scheduler;

import com.example.vote.service.VoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoteScheduler {
    
    private final VoteService voteService;
    
    @Scheduled(fixedRate = 60000)
    public void checkExpiredVotes() {
        log.debug("检查过期投票...");
        voteService.closeExpiredVotes();
    }
}
