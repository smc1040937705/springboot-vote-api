package com.example.vote.repository;

import com.example.vote.entity.VoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRecordRepository extends JpaRepository<VoteRecord, Long> {
    boolean existsByVoteIdAndVoterId(Long voteId, String voterId);
    
    List<VoteRecord> findByVoteId(Long voteId);
    
    long countByVoteId(Long voteId);
}
