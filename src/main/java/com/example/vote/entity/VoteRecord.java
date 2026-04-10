package com.example.vote.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vote_records")
public class VoteRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long voteId;
    
    @Column(nullable = false)
    private Long optionId;
    
    @Column(nullable = false)
    private String voterId;
    
    @Column(nullable = false)
    private LocalDateTime voteTime;
    
    @PrePersist
    public void prePersist() {
        if (voteTime == null) {
            voteTime = LocalDateTime.now();
        }
    }
}
