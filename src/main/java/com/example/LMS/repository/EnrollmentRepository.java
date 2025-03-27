package com.example.LMS.repository;

import com.example.LMS.model.Enrollment;
import com.example.LMS.model.Lesson;
import com.example.LMS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUser(User user);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    List<Enrollment> findByUserId(Long userId);

    void deleteByUserIdAndCourseId(Long userId, Long courseId);

    // Tìm kiếm Enrollment bằng userId và courseId
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    List<Enrollment> findAllByUserId(Long userId);

    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId")
    List<Lesson> getLessonsByCourseId(@Param("courseId") Long courseId);
}