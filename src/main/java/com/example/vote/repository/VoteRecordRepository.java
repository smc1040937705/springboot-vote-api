package com.example.vote.repository;

import com.example.vote.entity.VoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteRecordRepository extends JpaRepository<VoteRecord, Long> {

    boolean existsByVoteIdAndUserId(Long voteId, String userId);

    List<VoteRecord> findByVoteIdAndUserId(Long voteId, String userId);
}
