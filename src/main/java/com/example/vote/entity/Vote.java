package com.example.vote.entity;

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
    private Boolean anonymous;
    
    @Column(nullable = false)
    private Boolean multipleChoice;
    
    @Column(nullable = false)
    private Integer maxChoices;
    
    @Column(nullable = false)
    private LocalDateTime deadline;
    
    @Column(nullable = false)
    private Boolean closed;
    
    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteOption> options = new ArrayList<>();
    
    @PrePersist
    public void prePersist() {
        if (closed == null) {
            closed = false;
        }
        if (multipleChoice == null) {
            multipleChoice = false;
        }
        if (maxChoices == null) {
            maxChoices = 1;
        }
    }
}
