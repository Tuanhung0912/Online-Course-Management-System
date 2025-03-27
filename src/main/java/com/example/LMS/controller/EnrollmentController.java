package com.example.LMS.controller;

import com.example.LMS.model.Course;
import com.example.LMS.model.CustomUserDetail;
import com.example.LMS.model.Enrollment;
import com.example.LMS.service.CourseService;
import com.example.LMS.service.EmailService;
import com.example.LMS.service.EnrollmentService;
import com.example.LMS.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // Thực hiện chức năng tham gia khóa học
    @PostMapping("/enroll")
    public String enrollUser(@AuthenticationPrincipal CustomUserDetail customUserDetail,
                             @RequestParam("courseId") Long courseId,
                             RedirectAttributes redirectAttributes) {
        // Lấy ID người dùng từ CustomUserDetail
        Long userId = customUserDetail.getId();

        // Lấy thông tin người dùng từ UserService
        var user = userService.findById(userId);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Người dùng không tồn tại!");
            return "redirect:/login";
        }

        // Lấy thông tin khóa học
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            redirectAttributes.addFlashAttribute("error", "Khóa học không tồn tại!");
            return "redirect:/courses";
        }

        // Kiểm tra email trước khi gửi
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Người dùng không có email. Không thể gửi thông báo.");
            return "redirect:/courses";
        }

        // Thực hiện đăng ký (set các thông tin người dùng nhập vào form)
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(LocalDateTime.now());

        // Hàm lưu vào cơ sở dữ liệu
        enrollmentService.saveEnrollment(enrollment);

        // Chuẩn bị dữ liệu cho email
        Map<String, Object> variables = Map.of(
                "userName", user.getName(),
                "userEmail", user.getEmail(),
                "userPhone", user.getPhoneNumber() != null ? user.getPhoneNumber() : "Không có số điện thoại",
                "courseName", course.getName(),
                "courseDescription", course.getDescription(),
                "courseDuration", course.getDuration(),
                "courseStartDate", LocalDateTime.now().toString()
        );

        // Gửi email
        try {
            emailService.sendHtmlEmail(
                    user.getEmail(),
                    "Đăng ký khóa học thành công",
                    "email/course-enrollment.html", // Đường dẫn template
                    variables
            );
            redirectAttributes.addFlashAttribute("success", "Đăng ký khóa học thành công!");
        } catch (MessagingException e) {
            redirectAttributes.addFlashAttribute("error", "Không thể gửi email thông báo.");
        }

        return "redirect:/courses";
    }

    // Thực hiện chức năng xem các khóa học đã tham gia
    @GetMapping
    public String viewMyCourses(@AuthenticationPrincipal CustomUserDetail customUserDetail, Model model) {
        Long userId = customUserDetail.getId();
        var enrollments = enrollmentService.getEnrollmentsByUserId(userId);

        // Tính tổng thời gian học cho từng khóa học
        Map<Long, Long> totalStudyTimeMap = new HashMap<>();
        for (Enrollment enrollment : enrollments) {
            long totalStudyTime = enrollmentService.calculateTotalStudyTime(enrollment);
            totalStudyTimeMap.put(enrollment.getCourse().getId(), totalStudyTime);
        }

        model.addAttribute("enrollments", enrollments);
        model.addAttribute("totalStudyTimeMap", totalStudyTimeMap);
        return "enrollment/list";
    }

    // Thực hiện chức năng xóa một khóa học ra khỏi danh sách các khóa học đã tham gia
    @PostMapping("/delete")
    public String removeCourse(@AuthenticationPrincipal CustomUserDetail customUserDetail,
                               @RequestParam("courseId") Long courseId,
                               RedirectAttributes redirectAttributes) {
        // Lấy thông tin người dùng hiện tại
        Long userId = customUserDetail.getId();

        // Kiểm tra xem khóa học có trong danh sách của người dùng không
        boolean isEnrolled = enrollmentService.isUserEnrolledInCourse(userId, courseId);
        if (!isEnrolled) {
            redirectAttributes.addFlashAttribute("error", "Khóa học không tồn tại trong danh sách của bạn!");
            return "redirect:/enrollments";
        }

        // Xóa đăng ký khóa học và tiến trình học tập
        enrollmentService.removeEnrollmentAndProgress(userId, courseId);

        // Thông báo thành công
        redirectAttributes.addFlashAttribute("success", "Khóa học đã được xóa khỏi danh sách của bạn!");
        return "redirect:/enrollments";
    }


}
