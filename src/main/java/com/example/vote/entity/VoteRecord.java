package com.example.vote.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_vote_record", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"vote_id", "userId", "optionId"})
})
public class VoteRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long voteId;

    @Column(nullable = false)
    private Long optionId;

    private String userId;

    @Column(nullable = false)
    private LocalDateTime votedAt = LocalDateTime.now();
}
