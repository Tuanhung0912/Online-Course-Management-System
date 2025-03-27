package com.example.LMS.repository;

import com.example.LMS.model.Course;
import com.example.LMS.model.CourseCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // Tìm kiếm khóa học theo tên không phân biệt chữ hoa/chữ thường
    List<Course> findByNameContainingIgnoreCase(String name);

    List<Course> findByCourseCatalog_Id(Long catalogId);
}
