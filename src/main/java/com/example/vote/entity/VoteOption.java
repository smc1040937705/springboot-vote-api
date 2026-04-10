package com.example.vote.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "vote_options")
public class VoteOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String content;
    
    @Column(nullable = false)
    private Integer voteCount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;
    
    @PrePersist
    public void prePersist() {
        if (voteCount == null) {
            voteCount = 0;
        }
    }
}
