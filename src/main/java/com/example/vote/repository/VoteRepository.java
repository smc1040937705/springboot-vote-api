package com.example.vote.repository;

import com.example.vote.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("SELECT v FROM Vote v WHERE v.isClosed = false AND v.deadline <= :now")
    List<Vote> findExpiredVotes(LocalDateTime now);
}
