package com.example.LMS.service;


import com.example.LMS.model.Quiz;
import com.example.LMS.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizService {
    @Autowired
    private QuizRepository quizRepository;

    public List<Quiz> getQuizzesByCourseId(Long courseId) {
        return quizRepository.findByCourseId(courseId);
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    public Quiz saveQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id).orElse(null);
    }

    public void deleteQuiz(Long id) {
        quizRepository.deleteById(id);
    }

    public Long getCourseIdByQuizId(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz không tồn tại với ID: " + quizId));
        return quiz.getCourse().getId(); // Giả sử `Quiz` có thuộc tính `course`
    }


}
