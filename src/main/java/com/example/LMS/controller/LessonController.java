package com.example.LMS.controller;


import com.example.LMS.model.Course;
import com.example.LMS.model.CustomUserDetail;
import com.example.LMS.model.Lesson;
import com.example.LMS.model.Progress;
import com.example.LMS.service.CourseService;
import com.example.LMS.service.LessonService;
import com.example.LMS.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/lessons")
public class LessonController {
    @Autowired
    private LessonService lessonService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ProgressService progressService;

    
    private String saveVideoStatic(MultipartFile video) throws IOException {
        File saveFile = new ClassPathResource("static/videos").getFile();
        String fileName = UUID.randomUUID()+ "." + StringUtils.getFilenameExtension(video.getOriginalFilename());
        Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
        Files.copy(video.getInputStream(), path);
        return fileName;
    }

    // Thực hiện chức năng hiển thị form thêm bài học
    @GetMapping("/add/{courseId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showAddLessonForm(@PathVariable Long courseId, Model model) {
        Lesson lesson = new Lesson();
        model.addAttribute("lesson", lesson);
        model.addAttribute("courseId", courseId);
        return "lesson/add";
    }

    // Thực hiện chức năng thêm bài học
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String addLesson(@ModelAttribute("lesson") Lesson lesson, @RequestParam("courseId") Long courseId, @RequestParam("videoFile") MultipartFile videoFile) {
        Course course = courseService.getCourseById(courseId);
        lesson.setCourse(course);

        // Thiết lập thời gian tạo bài học là thời gian hiện tại
        lesson.setCreationTime(LocalDateTime.now());

        //Xử lý lưu tệp video
        if (!videoFile.isEmpty()) {
            try {
                String videoName = saveVideoStatic(videoFile);
                lesson.setVideoUrl("/videos/" +videoName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        lessonService.save(lesson);
        return "redirect:/courses/details/" + courseId;
    }

    @GetMapping("/list/{courseId}")
    public String showLessonList(@PathVariable Long courseId,
                                 @AuthenticationPrincipal CustomUserDetail customUserDetail,
                                 Model model) {
        List<Lesson> lessons = lessonService.getLessonsByCourseId(courseId);
        model.addAttribute("lessons", lessons);

        // Khởi tạo lessonCompletedMap
        Map<Long, Boolean> lessonCompletedMap = new HashMap<>();
        if (customUserDetail != null) {
            Long userId = customUserDetail.getId();
            List<Progress> progressList = progressService.getUserProgressInCourse(userId, courseId);

            for (Progress progress : progressList) {
                if (progress.getLesson() != null) {
                    lessonCompletedMap.put(progress.getLesson().getId(), progress.isCompleted());
                }
            }
        }

        // Thêm lessonCompletedMap vào model
        model.addAttribute("lessonCompletedMap", lessonCompletedMap);

        return "lesson/list";
    }


    // Thực hiện chức năng hiển thị form cập nhật bài học
    @GetMapping("/edit/{lessonId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showEditLessonForm(@PathVariable Long lessonId, Model model) {
        Lesson lesson = lessonService.getLessonById(lessonId);
        model.addAttribute("lesson", lesson);
        model.addAttribute("courseId", lesson.getCourse().getId());
        return "lesson/edit";
    }

    // Thực hiện chức năng cập nhật bài học
    @PostMapping("/edit/{lessonId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String editLesson(@PathVariable Long lessonId, @ModelAttribute("lesson") Lesson lesson, @RequestParam("courseId") Long courseId,  @RequestParam("videoFile") MultipartFile videoFile) {
        Lesson existingLesson = lessonService.getLessonById(lessonId);
        existingLesson.setName(lesson.getName());
        existingLesson.setDescription(lesson.getDescription());
        existingLesson.setCreationTime(LocalDateTime.now());

        //Xử lý lưu tệp video
        if (!videoFile.isEmpty()) {
            try {
                String videoName = saveVideoStatic(videoFile);
                existingLesson.setVideoUrl("/videos/" +videoName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        lessonService.save(existingLesson);
        return "redirect:/courses/details/" + courseId;
    }

    // Thực hiện xóa bài học
    @GetMapping("/delete/{lessonId}")
    public String deleteLesson(@PathVariable Long lessonId) {
        Lesson lesson = lessonService.getLessonById(lessonId);
        Long courseId = lesson.getCourse().getId();
        lessonService.deleteLesson(lessonId);
        return "redirect:/courses/details/" + courseId;
    }

    // Thực hiện chức năng hiển thị bài học getLessonsByCourseId
    /*@GetMapping("/list/{courseId}")
    public String showLesson(@PathVariable Long courseId, Model model) {
        List<Lesson> lessons = lessonService.getLessonsByCourseId(courseId);
        model.addAttribute("lessons", lessons);
        model.addAttribute("courseId", courseId);
        return "lesson/list";
    }*/

    /*@GetMapping("/list/{courseId}")
    public String showLessonList(@PathVariable Long courseId,
                                 @AuthenticationPrincipal CustomUserDetail customUserDetail,
                                 Model model) {
        List<Lesson> lessons = lessonService.getLessonsByCourseId(courseId);
        model.addAttribute("lessons", lessons);

        // Khởi tạo lessonCompletedMap
        Map<Long, Boolean> lessonCompletedMap = new HashMap<>();
        if (customUserDetail != null) {
            Long userId = customUserDetail.getId();

            // Lấy danh sách các bài học đã hoàn thành
            List<Progress> progressList = progressService.getUserProgressInCourse(userId, courseId);
            for (Progress progress : progressList) {
                if (progress.getLesson() != null) {
                    lessonCompletedMap.put(progress.getLesson().getId(), progress.isCompleted());
                }
            }
        }

        model.addAttribute("lessonCompletedMap", lessonCompletedMap);
        return "lesson/list";
    }*/
}
