package com.example.LMS.controller;


import com.example.LMS.model.*;
import com.example.LMS.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/quizzes")
public class QuizResultController {
    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizQuestionService quizQuestionService;

    @Autowired
    private QuizResultService quizResultService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @GetMapping("/attempt/{quizId}")
    public String attemptQuiz(@PathVariable Long quizId, Model model) {
        // Lấy thông tin user hiện tại từ Spring Security
        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetail.getId(); // Lấy ID của user từ CustomUserDetail

        Long courseId = courseService.getCourseIdByQuizId(quizId); // Lấy courseId liên kết với quizId

        Quiz quiz = quizService.getQuizById(quizId);
        List<QuizQuestion> questions = quizQuestionService.getQuizQuestionByQuizId(quizId);

        model.addAttribute("userId", userId); // Truyền userId vào model
        model.addAttribute("courseId", courseId); // Truyền courseId vào model
        model.addAttribute("quiz", quiz); // Truyền quiz vào model
        model.addAttribute("questions", questions); // Truyền danh sách câu hỏi vào model

        return "quiz/attempt";
    }

    @PostMapping("/submit")
    public String submitQuiz(@RequestParam(required = false) Long userId,
                             @RequestParam(required = false) Long courseId,
                             @RequestParam Long quizId,
                             @RequestParam Map<String, String> answers,
                             Model model) {
        if (userId == null || courseId == null) {
            throw new IllegalArgumentException("Thiếu thông tin userId hoặc courseId.");
        }

        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("Không có câu trả lời nào được gửi.");
        }

        System.out.println("Received answers: " + answers);

        List<QuizResult> results = new ArrayList<>();

        answers.forEach((key, selectedOption) -> {
            Long questionId;
            try {
                // Chỉ xử lý các key là số (ID câu hỏi)
                if (!key.matches("\\d+")) {
                    return; // Bỏ qua các key không hợp lệ
                }
                questionId = Long.valueOf(key);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID câu hỏi không hợp lệ: " + key, e);
            }

            QuizQuestion question = quizQuestionService.getQuestionById(questionId);
            if (question == null) {
                throw new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + questionId);
            }

            boolean isCorrect = question.getCorrectOption() == selectedOption.charAt(0);

            QuizResult result = new QuizResult();
            result.setUser(userService.findById(userId));
            result.setCourse(courseService.getCourseById(courseId));
            result.setQuiz(quizService.getQuizById(quizId));
            result.setQuizQuestion(question);
            result.setSelectedOption(selectedOption.charAt(0));
            result.setCorrect(isCorrect);
            result.setSubmitTime(LocalDateTime.now());
            quizResultService.saveQuizResult(result);
            results.add(result);
        });

        long correctCount = results.stream().filter(QuizResult::isCorrect).count();
        long totalQuestions = results.size();

        model.addAttribute("results", results);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("quiz", quizService.getQuizById(quizId));

        return "quiz/results";
    }

    @GetMapping("/results/{quizId}")
    public String viewResults(@PathVariable Long quizId, Model model) {
        // Lấy thông tin quiz
        Quiz quiz = quizService.getQuizById(quizId);
        if (quiz == null) {
            throw new IllegalArgumentException("Không tìm thấy bài quiz với ID: " + quizId);
        }

        // Lấy danh sách kết quả của quiz này
        List<QuizResult> results = quizResultService.getResultsByQuizId(quizId);

        // Tính toán số liệu thống kê
        long correctCount = results.stream().filter(QuizResult::isCorrect).count();
        long totalQuestions = results.size();

        // Thêm dữ liệu vào model
        model.addAttribute("quiz", quiz); // Thông tin quiz
        model.addAttribute("results", results); // Danh sách kết quả
        model.addAttribute("correctCount", correctCount); // Số câu đúng
        model.addAttribute("totalQuestions", totalQuestions); // Tổng số câu hỏi

        return "quiz/results"; // Trả về view kết quả
    }

    @GetMapping("/submissions/{quizId}")
    public String viewSubmissionsByQuiz(@PathVariable Long quizId, Model model) {
        // Lấy thông tin bài quiz
        Quiz quiz = quizService.getQuizById(quizId);
        if (quiz == null) {
            throw new IllegalArgumentException("Không tìm thấy bài quiz với ID: " + quizId);
        }

        // Lấy danh sách bài nộp liên quan đến quiz
        List<QuizResult> quizResults = quizResultService.getResultsByQuizId(quizId);

        // Lấy thông tin người dùng hiện tại từ SecurityContext
        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = userDetail.getId(); // ID của người dùng hiện tại
        boolean isAdmin = userDetail.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN")); // Kiểm tra vai trò ADMIN


        // Nhóm các kết quả theo từng người dùng (mỗi bài nộp chỉ trên 1 dòng)
        Map<Long, List<QuizResult>> submissionsGroupedByUser = quizResults.stream()
                .collect(Collectors.groupingBy(result -> result.getUser().getId()));

        // Tính số câu đúng và tổng số câu cho từng bài nộp
        Map<Long, String> correctAndTotalMap = submissionsGroupedByUser.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            long correctCount = entry.getValue().stream().filter(QuizResult::isCorrect).count();
                            long totalQuestions = entry.getValue().size();
                            return correctCount + "/" + totalQuestions;
                        }
                ));

        Map<Long, Boolean> canViewDetails = submissionsGroupedByUser.keySet().stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> isAdmin || userId.equals(currentUserId)
                ));

        // Truyền dữ liệu vào model
        model.addAttribute("quiz", quiz);
        model.addAttribute("submissionsGroupedByUser", submissionsGroupedByUser);
        model.addAttribute("correctAndTotalMap", correctAndTotalMap);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("canViewDetails", canViewDetails); // Map thể hiện quyền xem chi tiết


        return "quiz/submissions"; // Trả về file HTML submissions.html
    }

    @GetMapping("/details/{id}")
    public String viewSubmissionDetails(@PathVariable Long id, @RequestParam Long quizId, Model model) {
        // Lấy danh sách kết quả cho user trong bài quiz
        List<QuizResult> questionResults = quizResultService.getResultsByUserAndQuiz(id, quizId);

        // Lấy thông tin bài quiz
        Quiz quiz = quizService.getQuizById(quizId);

        // Tính toán số câu đúng và tổng số câu
        long correctCount = questionResults.stream().filter(QuizResult::isCorrect).count();
        long totalQuestions = questionResults.size();

        // Truyền dữ liệu vào model
        model.addAttribute("quiz", quiz);
        model.addAttribute("questionResults", questionResults);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("totalQuestions", totalQuestions);

        return "quiz/submission-details"; // Trả về view chi tiết bài nộp
    }
}
