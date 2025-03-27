package com.example.LMS.service;

import com.example.LMS.model.Quiz;
import com.example.LMS.model.QuizQuestion;
import com.example.LMS.repository.QuizQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizQuestionService {

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    public List<QuizQuestion> getQuizQuestionByQuizId(long quizId) {
        return quizQuestionRepository.findByQuizId(quizId);
    }

    public List<QuizQuestion> getQuestionsByQuizId(long quizId) {
        return quizQuestionRepository.findByQuizId(quizId);
    }

    public QuizQuestion saveQuestion(QuizQuestion quizQuestion) {
        return quizQuestionRepository.save(quizQuestion);
    }

    public void deleteQuestion(long id) {
        quizQuestionRepository.deleteById(id);
    }

    public QuizQuestion getQuestionById(Long id) {
        return quizQuestionRepository.findById(id).orElse(null);
    }

    public int countQuestionsByQuizId(Long quizId) {
        return quizQuestionRepository.countByQuizId(quizId);
    }
}
