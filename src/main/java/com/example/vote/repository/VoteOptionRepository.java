package com.example.vote.repository;

import com.example.vote.entity.VoteOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteOptionRepository extends JpaRepository<VoteOption, Long> {
    List<VoteOption> findByVoteId(Long voteId);
}
