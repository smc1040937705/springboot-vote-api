package com.vote.repository;

import com.vote.entity.VoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRecordRepository extends JpaRepository<VoteRecord, Long> {
    Optional<VoteRecord> findByVoteIdAndUserId(Long voteId, String userId);
    
    boolean existsByVoteIdAndUserId(Long voteId, String userId);
    
    List<VoteRecord> findByVoteId(Long voteId);
    
    long countByOptionId(Long optionId);
}
