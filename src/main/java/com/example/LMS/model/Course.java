package com.example.LMS.model;

import com.example.LMS.validator.ValidCourseCatalogId;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100, message = "Tên phải ít hơn 100 ký tự")
    @Column(name = "name")
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "courseCatalog_id")
    @ValidCourseCatalogId
    private CourseCatalog courseCatalog;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    // Thuộc tính mới để lưu trữ đường dẫn hoặc URL của ảnh
    @Column(name = "imageData", length = 500)
    private String imageData;

    @Column(name = "duration")
    private int duration;

    @Column(name = "creationTime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creationTime;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizResult> quizResults;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;
}

/*@Transient
    private double progress;*/