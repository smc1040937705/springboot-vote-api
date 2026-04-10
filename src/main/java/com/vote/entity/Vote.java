package com.vote.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "votes")
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private LocalDateTime deadline;
    
    @Column(nullable = false)
    private boolean multipleChoice;
    
    @Column(nullable = false)
    private Integer maxChoices = 1;
    
    @Column(nullable = false)
    private boolean anonymous;
    
    @Column(nullable = false)
    private boolean closed = false;
    
    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteOption> options = new ArrayList<>();
    
    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL)
    private List<VoteRecord> records = new ArrayList<>();
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(deadline);
    }
}
