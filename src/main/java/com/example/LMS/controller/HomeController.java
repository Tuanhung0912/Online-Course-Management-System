package com.example.LMS.controller;

import com.example.LMS.model.CourseCatalog;
import com.example.LMS.service.CommentService;
import com.example.LMS.service.CourseCatalogService;
import com.example.LMS.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class HomeController {
    @Autowired
    private CourseService courseService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CourseCatalogService courseCatalogService;

    @GetMapping
    public String home(Model model) {
        // Lấy 3 khóa học có điểm trung bình sao cao nhất
        var topRatedCourses = courseService.getTopRatedCourses(3);

        // Tạo Map lưu dữ liệu điểm trung bình và số lượng đánh giá cho từng khóa học
        var courseRatings = new HashMap<Long, Map<String, Object>>();
        for (var course : topRatedCourses) {
            var ratingData = commentService.getAverageRatingAndCountByCourseId(course.getId());
            courseRatings.put(course.getId(), ratingData);
        }
        List<CourseCatalog> topCourseCatalogs = courseCatalogService.getTopCourseCatalogs(3);


        // Truyền dữ liệu vào model
        model.addAttribute("topRatedCourses", topRatedCourses);
        model.addAttribute("courseRatings", courseRatings);
        model.addAttribute("topCourseCatalogs", topCourseCatalogs);

        return "home/index"; // Tên file Thymeleaf cho trang chủ
    }

}
