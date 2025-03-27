package com.example.LMS.service;


import com.example.LMS.model.Course;
import com.example.LMS.model.Quiz;
import com.example.LMS.repository.CourseRepository;
import com.example.LMS.repository.LessonRepository;
import com.example.LMS.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CommentService commentService;


    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    public void addCourse(Course course) {
        courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    public void updateCourse(Course course) {
        courseRepository.save(course);
    }

    // Tìm kiếm khóa học theo tên
    public List<Course> searchCoursesByName(String name) {
        return courseRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Course> getCoursesByCatalog(Long catalogId) {
        return courseRepository.findByCourseCatalog_Id(catalogId);
    }

    public Long getCourseIdByQuizId(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz không tồn tại với ID: " + quizId));

        System.out.println("Quiz ID: " + quizId + ", Course ID: " + quiz.getCourse().getId());
        return quiz.getCourse().getId();
    }

    public List<Course> getTopRatedCourses(int limit) {
        List<Course> allCourses = courseRepository.findAll();

        // Tính điểm trung bình sao và sắp xếp các khóa học theo thứ tự giảm dần của điểm trung bình sao
        return allCourses.stream()
                .sorted((c1, c2) -> {
                    Map<String, Object> ratingData1 = commentService.getAverageRatingAndCountByCourseId(c1.getId());
                    Map<String, Object> ratingData2 = commentService.getAverageRatingAndCountByCourseId(c2.getId());

                    double avg1 = (double) ratingData1.get("averageRating");
                    double avg2 = (double) ratingData2.get("averageRating");

                    return Double.compare(avg2, avg1); // Sắp xếp giảm dần
                })
                .limit(limit) // Lấy số lượng giới hạn
                .toList();
    }

}
