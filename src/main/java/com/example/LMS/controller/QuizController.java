package com.example.LMS.controller;

import com.example.LMS.model.Course;
import com.example.LMS.model.Quiz;
import com.example.LMS.model.QuizQuestion;
import com.example.LMS.service.CourseService;
import com.example.LMS.service.QuizQuestionService;
import com.example.LMS.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quizzes")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private QuizQuestionService quizQuestionService;

    @GetMapping("/list/{courseId}")
    public String listQuizzes(@PathVariable Long courseId, Model model) {
        List<Quiz> quizzes = quizService.getQuizzesByCourseId(courseId);
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("courseId", courseId);

        // Đếm số lượng câu hỏi cho từng quiz
        Map<Long, Integer> questionCounts = new HashMap<>();
        for (Quiz quiz : quizzes) {
            int count = quizQuestionService.countQuestionsByQuizId(quiz.getId());
            questionCounts.put(quiz.getId(), count);
        }
        model.addAttribute("questionCounts", questionCounts);

        return "quiz/list"; // Trả về template Thymeleaf
    }


    @GetMapping("/add/{courseId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String addQuizForm(@PathVariable Long courseId, Model model) {
        model.addAttribute("quiz", new Quiz());
        model.addAttribute("courseId", courseId);
        return "quiz/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String saveQuiz(@RequestParam("courseId") Long courseId, @ModelAttribute Quiz quiz, RedirectAttributes redirectAttributes) {
        if (courseId == null) {
            redirectAttributes.addFlashAttribute("error", "Course ID is missing or invalid.");
            return "redirect:/courses"; // Redirect về danh sách khóa học
        }
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            redirectAttributes.addFlashAttribute("error", "Course not found.");
            return "redirect:/courses";
        }

        quiz.setCourse(course);
        // Thiết lập thời gian tạo bài trắc nghiệm là thời gian hiện tại
        quiz.setCreationTime(LocalDateTime.now());
        quizService.saveQuiz(quiz);
        return "redirect:/quizzes/list/" + courseId;
    }


    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String editQuizForm(@PathVariable Long id, Model model) {
        Quiz quiz = quizService.getQuizById(id);
        model.addAttribute("quiz", quiz);
        model.addAttribute("courseId", quiz.getCourse().getId());
        return "quiz/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateQuiz(@PathVariable Long id, @ModelAttribute("quiz") Quiz quiz, @RequestParam("courseId") Long courseId) {
        Quiz existingQuiz = quizService.getQuizById(id);
        existingQuiz.setTitle(quiz.getTitle());
        existingQuiz.setDescription(quiz.getDescription());
        existingQuiz.setDuration(quiz.getDuration());
        existingQuiz.setCreationTime(LocalDateTime.now());
        quizService.saveQuiz(existingQuiz);
        return "redirect:/quizzes/list/" + courseId;
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteQuiz(@PathVariable Long id) {
        Quiz quiz = quizService.getQuizById(id);
        Long courseId = quiz.getCourse().getId();
        quizService.deleteQuiz(id);
        return "redirect:/quizzes/list/" + courseId;
    }
}