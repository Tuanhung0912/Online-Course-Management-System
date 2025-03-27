package com.example.LMS.controller;


import com.example.LMS.model.Quiz;
import com.example.LMS.model.QuizQuestion;
import com.example.LMS.service.QuizQuestionService;
import com.example.LMS.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/quiz-questions")
public class QuizQuestionController {
    @Autowired
    private QuizQuestionService quizQuestionService;

    @Autowired
    private QuizService quizService;


    @GetMapping("/list/{quizId}")
    public String getQuestionsByQuiz(@PathVariable Long quizId, Model model) {
        Quiz quiz = quizService.getQuizById(quizId);
        List<QuizQuestion> quizQuestions = quizQuestionService.getQuestionsByQuizId(quizId);
        model.addAttribute("quizQuestions", quizQuestions);
        model.addAttribute("quizId", quizId);
        model.addAttribute("courseId", quiz.getCourse().getId());
        return "quiz-questions/list";
    }

    @GetMapping("/add/{quizId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showAddQuestionForm(@PathVariable Long quizId, Model model) {
        model.addAttribute("quizQuestion", new QuizQuestion());
        model.addAttribute("quizId", quizId);
        return "quiz-questions/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String addQuestion(@RequestParam("quizId") Long quizId, @ModelAttribute QuizQuestion quizQuestion) {
        Quiz quiz = quizService.getQuizById(quizId);
        quizQuestion.setQuiz(quiz);
        // Thiết lập thời gian tạo bài trắc nghiệm là thời gian hiện tại
        quizQuestion.setCreationTime(LocalDateTime.now());
        quizQuestionService.saveQuestion(quizQuestion);
        return "redirect:/quiz-questions/list/" + quizId;
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String editQuestion(@PathVariable Long id, Model model) {
        QuizQuestion quizQuestion = quizQuestionService.getQuestionById(id);
        model.addAttribute("quizQuestion", quizQuestion);
        model.addAttribute("quizId", quizQuestion.getQuiz().getId());
        return "quiz-questions/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateQuestion(@PathVariable Long id, @ModelAttribute("quizQuestion") QuizQuestion quizQuestion, @RequestParam("quizId") Long quizId) {
        QuizQuestion existingQuizQuestion = quizQuestionService.getQuestionById(id);
        existingQuizQuestion.setContent(quizQuestion.getContent());
        existingQuizQuestion.setOptionA(quizQuestion.getOptionA());
        existingQuizQuestion.setOptionB(quizQuestion.getOptionB());
        existingQuizQuestion.setOptionC(quizQuestion.getOptionC());
        existingQuizQuestion.setOptionD(quizQuestion.getOptionD());
        existingQuizQuestion.setCorrectOption(quizQuestion.getCorrectOption());
        existingQuizQuestion.setCreationTime(LocalDateTime.now());
        quizQuestionService.saveQuestion(existingQuizQuestion);
        return "redirect:/quiz-questions/list/" + quizId;
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteQuestion(@PathVariable Long id) {
        QuizQuestion quizQuestion = quizQuestionService.getQuestionById(id);
        Long quizId = quizQuestion.getQuiz().getId();
        quizQuestionService.deleteQuestion(id);
        return "redirect:/quiz-questions/list/" + quizId;
    }
}
