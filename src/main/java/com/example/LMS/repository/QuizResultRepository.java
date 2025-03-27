package com.example.LMS.repository;

import com.example.LMS.model.QuizResult;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByQuiz_Id(Long quizId);

    @Query("SELECT r FROM QuizResult r WHERE r.user.id = :userId AND r.quiz.id = :quizId")
    List<QuizResult> findByUserAndQuiz(@Param("userId") Long userId, @Param("quizId") Long quizId);

    @Query("SELECT COUNT(qr) FROM QuizResult qr WHERE qr.quiz.id = :quizId AND qr.user.id = :userId AND qr.isCorrect = true")
    int countCorrectAnswersByQuizAndUser(@Param("quizId") Long quizId, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM QuizResult qr WHERE qr.user.id = :userId AND qr.course.id = :courseId")
    void deleteByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
}
