package com.example.LMS.model;


import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "quiz_question")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", length = 1000)
    private String content;

    @Column(name = "option_a", length = 250)
    private String optionA;

    @Column(name = "option_b", length = 250)
    private String optionB;

    @Column(name = "option_c", length = 250)
    private String optionC;

    @Column(name = "option_d", length = 250)
    private String optionD;

    @Column(name = "correct_option", length = 1)
    private char correctOption; // A, B, C, D

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(mappedBy = "quizQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizResult> quizResults;

    @Column(name = "creationTime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creationTime;

}
