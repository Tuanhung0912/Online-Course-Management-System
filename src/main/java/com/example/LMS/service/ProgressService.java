package com.example.LMS.service;

import com.example.LMS.model.Progress;
import com.example.LMS.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgressService {
    @Autowired
    private ProgressRepository progressRepository;

    public List<Progress> getUserProgressInCourse(Long userId, Long courseId) {
        return progressRepository.findByUserIdAndLessonCourseId(userId, courseId);
    }

    public Progress getUserProgressInLesson(Long userId, Long lessonId) {
        return progressRepository.findByUserIdAndLessonId(userId, lessonId);
    }

    public Progress saveProgress(Progress progress) {
        return progressRepository.save(progress);
    }

    // Xóa toàn bộ tiến trình học tập của người dùng trong một khóa học
    public void deleteProgressByUserAndCourse(Long userId, Long courseId) {
        progressRepository.deleteByUserIdAndCourseId(userId, courseId);
    }
}
