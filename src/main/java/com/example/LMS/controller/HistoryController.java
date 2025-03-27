package com.example.LMS.controller;

import com.example.LMS.model.Course;
import com.example.LMS.model.CustomUserDetail;
import com.example.LMS.model.Lesson;
import com.example.LMS.model.Quiz;
import com.example.LMS.service.EnrollmentService;
import com.example.LMS.service.QuizResultService;
import com.example.LMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/history")
public class HistoryController {

    @Autowired
    private QuizResultService quizResultService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String getUserHistory(@AuthenticationPrincipal CustomUserDetail customUserDetail, Model model) {
        Long userId = customUserDetail.getId();

        // Lấy danh sách bài học hoàn thành
        Map<Course, List<Lesson>> completedLessons = enrollmentService.getCompletedLessonsByUser(userId);

        // Lấy trạng thái hoàn thành của khóa học
        Map<Course, Boolean> courseCompletionMap = new HashMap<>();
        Map<Course, LocalDateTime> courseCompletionDates = new HashMap<>();
        Map<Long, Boolean> quizCompletedMap = new HashMap<>();
        Map<Course, String> totalStudyTimeFormatted = new HashMap<>(); // Thay đổi Map để lưu chuỗi định dạng

        for (Course course : completedLessons.keySet()) {
            boolean isCourseCompleted = enrollmentService.isCourseCompleted(userId, course.getId());
            courseCompletionMap.put(course, isCourseCompleted);

            if (isCourseCompleted) {
                boolean allQuizzesCompleted = course.getQuizzes().stream()
                        .allMatch(quiz -> quizResultService.isQuizCompletedByUser(quiz.getId(), userId));

                // Chỉ tính thời gian học và ngày hoàn thành nếu tất cả bài học và bài tập trắc nghiệm đều hoàn thành
                if (allQuizzesCompleted) {
                    LocalDateTime completionDate = enrollmentService.getCourseCompletionDate(userId, course.getId());
                    String formattedStudyTime = enrollmentService.calculateTotalStudyTimeForCourseFormatted(userId, course.getId()); // Gọi hàm mới

                    courseCompletionDates.put(course, completionDate);
                    totalStudyTimeFormatted.put(course, formattedStudyTime);
                }
            }

            // Tính toán trạng thái hoàn thành cho các bài tập trắc nghiệm
            for (Quiz quiz : course.getQuizzes()) {
                boolean isQuizCompleted = quizResultService.isQuizCompletedByUser(quiz.getId(), userId);
                quizCompletedMap.put(quiz.getId(), isQuizCompleted);
            }
        }

        model.addAttribute("completedLessons", completedLessons);
        model.addAttribute("courseCompletionMap", courseCompletionMap);
        model.addAttribute("courseCompletionDates", courseCompletionDates);
        model.addAttribute("totalStudyTime", totalStudyTimeFormatted); // Truyền Map định dạng giờ: phút
        model.addAttribute("quizCompletedMap", quizCompletedMap);

        return "history/list";
    }


}

