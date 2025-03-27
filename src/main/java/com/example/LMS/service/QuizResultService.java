package com.example.LMS.service;

import com.example.LMS.model.QuizResult;
import com.example.LMS.repository.QuizResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuizResultService {
    @Autowired
    private QuizResultRepository quizResultRepository;

    public void saveQuizResult(QuizResult quizResult) {
        quizResultRepository.save(quizResult);
    }

    // Xóa toàn bộ kết quả bài tập trắc nghiệm của người dùng trong một khóa học
    public void deleteQuizResultsByUserAndCourse(Long userId, Long courseId) {
        quizResultRepository.deleteByUserIdAndCourseId(userId, courseId);
    }

    public List<QuizResult> getResultsByQuizId(Long quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống");
        }
        return quizResultRepository.findByQuiz_Id(quizId);
    }

    public List<QuizResult> getAllQuizResults() {
        return quizResultRepository.findAll();
    }

    public List<QuizResult> getResultsByUserAndQuiz(Long userId, Long quizId) {
        return quizResultRepository.findByUserAndQuiz(userId, quizId);
    }

    public boolean isQuizCompletedByUser(Long quizId, Long userId) {
        // Kiểm tra nếu người dùng đã hoàn thành tất cả câu hỏi trong bài tập
        return quizResultRepository.countCorrectAnswersByQuizAndUser(quizId, userId) > 0;
    }

}
