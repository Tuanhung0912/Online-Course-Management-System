package com.example.LMS.repository;


import com.example.LMS.model.Progress;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByUserIdAndLessonCourseId(Long userId, Long courseId);
    Progress findByUserIdAndLessonId(Long userId, Long lessonId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Progress p WHERE p.user.id = :userId AND p.lesson.course.id = :courseId")
    void deleteByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

}
