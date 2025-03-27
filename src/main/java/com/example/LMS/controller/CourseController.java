package com.example.LMS.controller;


import com.example.LMS.model.*;
import com.example.LMS.repository.CommentRepository;
import com.example.LMS.repository.ReplyRepository;
import com.example.LMS.repository.UserRepository;
import com.example.LMS.service.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Controller
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private QuizResultService quizResultService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseCatalogService courseCatalogService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReplyService replyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReplyRepository replyRepository;


    //Hiển thị danh sách khóa học
    @GetMapping
    public String showAllCourses(Model model) {
        List<Course> courses = courseService.getAllCourses();
        List<CourseCatalog> courseCatalogs = courseCatalogService.getAllCourseCatalog();

        // Thêm dữ liệu trung bình rating và số lượng đánh giá cho mỗi khóa học
        Map<Long, Map<String, Object>> courseRatings = new HashMap<>();
        for (Course course : courses) {
            Map<String, Object> ratingData = commentService.getAverageRatingAndCountByCourseId(course.getId());
            courseRatings.put(course.getId(), ratingData);
        }

        // Tạo một map lưu thông tin người tạo khóa học
        Map<Long, String> courseCreators = new HashMap<>();
        for (Course course : courses) {
            User creator = course.getCreatedBy(); // Lấy thông tin người tạo
            courseCreators.put(course.getId(), creator != null ? creator.getUsername() : "Đang cập nhật");
        }

        model.addAttribute("courses", courses);
        model.addAttribute("courseCreators", courseCreators);
        model.addAttribute("courseRatings", courseRatings);
        model.addAttribute("courseCatalogs", courseCatalogs);
        return "course/list";
    }

    // Hàm lưu file ảnh
    private String saveImageStatic(MultipartFile image) throws IOException {
        File saveFile = new ClassPathResource("static/images").getFile(); // Khởi tạo đường dẫn file ảnh
        String fileName = UUID.randomUUID()+ "." + StringUtils.getFilenameExtension(image.getOriginalFilename());
        Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
        Files.copy(image.getInputStream(), path);
        return fileName;
    }

    //Hiển thị form thêm khóa học
    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String addCourseForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("courseCatalogs", courseCatalogService.getAllCourseCatalog());
        return "course/add";
    }

    //Thực hiện chức năng thêm khóa học
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String addCourse(@Valid @ModelAttribute("course") Course course, BindingResult result, @RequestParam("image") MultipartFile imageFile, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("courseCatalogs", courseCatalogService.getAllCourseCatalog());
            return "course/add";
        }

        // Thiết lập thời gian tạo khóa học là thời gian hiện tại
        course.setCreationTime(LocalDateTime.now());

        // Lấy thông tin người dùng hiện tại từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Lấy username từ context
        User currentUser = userService.findByUsername(username); // Lấy thông tin User từ database
        course.setCreatedBy(currentUser);

        // Xử lý lưu file ảnh
        if (!imageFile.isEmpty()) {
            try {
                String imageName = saveImageStatic(imageFile);
                course.setImageData("/images/" +imageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        courseService.addCourse(course);
        return "redirect:/courses";
    }

    //Hiển thị form sửa thông tin khóa học
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String editCourseForm(@PathVariable("id") Long id, Model model) {
        Course course = courseService.getCourseById(id);
        if (course != null) {
            model.addAttribute("course", course);
            model.addAttribute("courseCatalogs", courseCatalogService.getAllCourseCatalog());
            return "course/edit";
        }

        return "redirect:/courses";
    }

    //Thực hiện chức năng thêm khóa học
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateCourse(@PathVariable("id") Long id, @Valid @ModelAttribute("course") Course course, @RequestParam("image") MultipartFile imageFile, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("courseCatalogs", courseCatalogService.getAllCourseCatalog());
            return "course/edit";
        }

        //Lấy dữ liệu của khóa học bằng mã
        Course existingCourse = courseService.getCourseById(id);

        //Gán lại các thuộc tính cho khóa học sau khi sửa
        existingCourse.setName(course.getName());
        existingCourse.setDescription(course.getDescription());
        existingCourse.setCourseCatalog(course.getCourseCatalog());

        // Lấy thông tin người dùng hiện tại từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Lấy username từ context
        User currentUser = userService.findByUsername(username); // Lấy thông tin User từ database
        existingCourse.setCreatedBy(currentUser);

        // Xử lý lưu file ảnh
        if (!imageFile.isEmpty()) {
            try {
                String imageName = saveImageStatic(imageFile);
                existingCourse.setImageData("/images/" + imageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Cập nhật lại khóa học
        courseService.updateCourse(existingCourse);
        return "redirect:/courses";
    }


    // Thực hiện chức năng xóa khóa học
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteCourse(@PathVariable("id") Long id) {
        courseService.deleteCourse(id);
        return "redirect:/courses";
    }

    @GetMapping("/details/{id}")
    public String showCourseDetail(@PathVariable Long id, Model model,
                                   @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            model.addAttribute("error", "Khóa học không tồn tại!");
            return "error";
        }

        // Lấy thông tin người tạo
        User createdBy = course.getCreatedBy();
        String creatorName = createdBy != null ? createdBy.getUsername() : "Đang cập nhật";

        boolean isEnrolled = false;
        long totalStudyTime = 0;
        Map<Long, Boolean> lessonCompletedMap = new HashMap<>();
        Map<Long, Boolean> quizCompletedMap = new HashMap<>();
        int lessonProgressPercent = 0; // Tiến trình bài học
        int quizProgressPercent = 0;   // Tiến trình bài tập trắc nghiệm
        int totalProgressPercent = 0; // Tiến trình tổng hợp


        if (customUserDetail != null) {
            Long userId = customUserDetail.getId();
            isEnrolled = enrollmentService.isUserEnrolledInCourse(userId, id);

            if (isEnrolled) {
                Enrollment enrollment = enrollmentService.getEnrollmentByUserAndCourse(userId, id);
                totalStudyTime = enrollmentService.calculateTotalStudyTime(enrollment);

                // Tính tiến trình bài học
                List<Progress> progressList = progressService.getUserProgressInCourse(userId, id);
                int completedLessons = 0;

                // Đánh dấu bài học hoàn thành hoặc chưa hoàn thành
                for (Lesson lesson : course.getLessons()) {
                    boolean isCompleted = progressList.stream()
                            .anyMatch(progress -> progress.getLesson() != null // Kiểm tra Lesson có null hay không
                                    && progress.getLesson().getId() == lesson.getId()
                                    && progress.isCompleted());
                    lessonCompletedMap.put(lesson.getId(), isCompleted);
                    if (isCompleted) {
                        completedLessons++;
                    }
                }

                int totalLessons = course.getLessons().size();
                lessonProgressPercent = totalLessons > 0 ? (completedLessons * 100 / totalLessons) : 0;

                // Tính tiến trình bài tập trắc nghiệm
                List<Quiz> quizzes = quizService.getQuizzesByCourseId(id);
                for (Quiz quiz : quizzes) {
                    boolean isQuizCompleted = quizResultService.isQuizCompletedByUser(quiz.getId(), userId);
                    quizCompletedMap.put(quiz.getId(), isQuizCompleted);
                }

                int completedQuizzes = 0;

                for (Quiz quiz : quizzes) {
                    boolean isQuizCompleted = quizResultService.isQuizCompletedByUser(quiz.getId(), userId);
                    quizCompletedMap.put(quiz.getId(), isQuizCompleted);
                    if (isQuizCompleted) {
                        completedQuizzes++;
                    }
                }
                int totalQuizzes = quizzes.size();
                quizProgressPercent = totalQuizzes > 0 ? (completedQuizzes * 100 / totalQuizzes) : 0;

                // Tính tiến trình tổng hợp
                int totalElements = totalLessons + totalQuizzes;
                int completedElements = completedLessons + completedQuizzes;
                totalProgressPercent = totalElements > 0 ? (completedElements * 100 / totalElements) : 0;

            }
        }

        // Lấy danh sách bình luận
        List<Comment> comments = commentService.getCommentsByCourse(id);


        // Đếm số lượng bình luận
        int commentCount = comments.size();

        // Thêm vào model để truyền sang view
        model.addAttribute("course", course);
        model.addAttribute("isEnrolled", isEnrolled);
        model.addAttribute("lessonCompletedMap", lessonCompletedMap);
        model.addAttribute("quizCompletedMap", quizCompletedMap);
        model.addAttribute("lessonProgressPercent", lessonProgressPercent);
        model.addAttribute("quizProgressPercent", quizProgressPercent);
        model.addAttribute("totalProgressPercent", totalProgressPercent);
        model.addAttribute("totalStudyTime", totalStudyTime);
        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new Comment());
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("creatorName", creatorName);


        return "course/details";
    }


    @PostMapping("/details/comments/reply/{commentId}")
    public String addReply(@PathVariable Long commentId,
                           @RequestParam String content,
                           Principal principal) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bình luận"));

        User user = userRepository.findByUsername(principal.getName());

        Reply reply = new Reply();
        reply.setContent(content);
        reply.setComment(comment);
        reply.setUser(user);
        reply.setCreatedAt(LocalDateTime.now());

        replyService.saveReply(reply);

        return "redirect:/courses/details/" + comment.getCourse().getId();
    }

    @PostMapping("/details/comments/reply/edit/{replyId}")
    public String editReply(@PathVariable Long replyId,
                            @RequestParam String content,
                            Principal principal) {
        // Lấy câu trả lời từ ID
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu trả lời"));

        // Kiểm tra người dùng có quyền chỉnh sửa không
        if (!reply.getUser().getUsername().equals(principal.getName())) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa câu trả lời này");
        }

        // Cập nhật nội dung
        reply.setContent(content);
        reply.setCreatedAt(LocalDateTime.now());
        replyRepository.save(reply);

        // Trả về trang chi tiết khóa học
        return "redirect:/courses/details/" + reply.getComment().getCourse().getId();
    }

    @GetMapping("/details/comments/reply/delete/{replyId}")
    public String deleteReply(@PathVariable Long replyId, Principal principal) {
        // Lấy câu trả lời từ ID
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu trả lời"));

        // Kiểm tra người dùng có quyền xóa không
        if (!reply.getUser().getUsername().equals(principal.getName())) {
            throw new SecurityException("Bạn không có quyền xóa câu trả lời này");
        }

        // Lưu lại Course ID để redirect
        Long courseId = reply.getComment().getCourse().getId();

        // Xóa câu trả lời
        replyRepository.delete(reply);

        // Trả về trang chi tiết khóa học
        return "redirect:/courses/details/" + courseId;
    }


    @PostMapping("/details/comments/{id}")
    public String addComment(@PathVariable Long id,
                             @RequestParam("content") String content,
                             @RequestParam("rating") int rating,
                             Principal principal) {
        User user = userService.findByUsername(principal.getName());
        Course course = courseService.getCourseById(id);

        Comment newComment = new Comment();
        newComment.setContent(content);
        newComment.setRating(rating); // Lưu đánh giá
        newComment.setUser(user);
        newComment.setCourse(course);

        commentService.saveComment(newComment);
        return "redirect:/courses/details/" + id;
    }


    @PostMapping("/details/comments/edit/{commentId}")
    public String editComment(@PathVariable Long commentId,
                              @RequestParam("content") String newContent,
                              Principal principal) {
        Comment comment = commentService.getCommentById(commentId);
        if (!comment.getUser().getUsername().equals(principal.getName())) {
            throw new SecurityException("Bạn không có quyền sửa bình luận này!");
        }
        comment.setContent(newContent);
        commentService.saveComment(comment);
        return "redirect:/courses/details/" + comment.getCourse().getId();
    }


    @GetMapping("/details/comments/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId, Principal principal) {
        Comment comment = commentService.getCommentById(commentId);
        if (!comment.getUser().getUsername().equals(principal.getName())) {
            throw new SecurityException("Bạn không có quyền xóa bình luận này!");
        }
        Long courseId = comment.getCourse().getId();
        commentService.deleteComment(commentId);
        return "redirect:/courses/details/" + courseId;
    }


    // Thực hiện chức năng tìm kiếm khóa học theo tên
    @GetMapping("/search")
    public String searchCourses(@RequestParam("keyword") String keyword, Model model) {
        List<Course> courses = courseService.searchCoursesByName(keyword);
        List<CourseCatalog> courseCatalogs = courseCatalogService.getAllCourseCatalog();


        // Thêm dữ liệu trung bình rating và số lượng đánh giá cho mỗi khóa học
        Map<Long, Map<String, Object>> courseRatings = new HashMap<>();
        for (Course course : courses) {
            Map<String, Object> ratingData = commentService.getAverageRatingAndCountByCourseId(course.getId());
            courseRatings.put(course.getId(), ratingData);
        }

        // Tạo một map lưu thông tin người tạo khóa học
        Map<Long, String> courseCreators = new HashMap<>();
        for (Course course : courses) {
            User creator = course.getCreatedBy(); // Lấy thông tin người tạo
            courseCreators.put(course.getId(), creator != null ? creator.getUsername() : "Đang cập nhật");
        }

        model.addAttribute("courses", courses);
        model.addAttribute("keyword", keyword);
        model.addAttribute("courseRatings", courseRatings);
        model.addAttribute("courseCreators", courseCreators);
        model.addAttribute("courseCatalogs", courseCatalogs);
        return "course/list";
    }

    // Thực hiện chức năng tìm kiếm khóa học theo danh mục
    @GetMapping("/searchByCatalog")
    public String searchCoursesByCatalog(@RequestParam(value = "catalogId", required = false) Long catalogId, Model model) {
        List<Course> courses;

        if (catalogId == null || catalogId == 0) {
            // Lấy toàn bộ danh sách khóa học nếu không chọn danh mục hoặc chọn "Tất cả"
            courses = courseService.getAllCourses();

        } else {
            // Lấy danh sách khóa học theo danh mục
            courses = courseService.getCoursesByCatalog(catalogId);
        }
        // Lấy dữ liệu của danh sách danh mục
        List<CourseCatalog> courseCatalogs = courseCatalogService.getAllCourseCatalog();

        // Thêm dữ liệu trung bình rating và số lượng đánh giá cho mỗi khóa học
        Map<Long, Map<String, Object>> courseRatings = new HashMap<>();
        for (Course course : courses) {
            Map<String, Object> ratingData = commentService.getAverageRatingAndCountByCourseId(course.getId());
            courseRatings.put(course.getId(), ratingData);
        }

        // Tạo một map lưu thông tin người tạo khóa học
        Map<Long, String> courseCreators = new HashMap<>();
        for (Course course : courses) {
            User creator = course.getCreatedBy(); // Lấy thông tin người tạo
            courseCreators.put(course.getId(), creator != null ? creator.getUsername() : "Đang cập nhật");
        }

        // Đổ dữ liệu lên view
        model.addAttribute("courses", courses);
        model.addAttribute("courseRatings", courseRatings);
        model.addAttribute("courseCatalogs", courseCatalogs);
        model.addAttribute("selectedCatalogId", catalogId); // Giữ lại danh mục đã chọn
        model.addAttribute("courseCreators", courseCreators);
        return "course/list";
    }

    @PostMapping("/details/{courseId}/lesson/{lessonId}/incomplete")
    public String markLessonAsIncomplete(@PathVariable Long courseId,
                                         @PathVariable Long lessonId,
                                         @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        if (customUserDetail == null) {
            return "redirect:/login";
        }

        Long userId = customUserDetail.getId();
        Progress progress = progressService.getUserProgressInLesson(userId, lessonId);

        // Nếu tiến trình tồn tại, cập nhật lại trạng thái chưa hoàn thành
        if (progress != null && progress.isCompleted()) {
            progress.setCompleted(false);
            progress.setCompletedAt(null);
            progressService.saveProgress(progress);
        }

        return "redirect:/courses/details/" + courseId;
    }

    @PostMapping("/details/{courseId}/certificate")
    public ResponseEntity<byte[]> generateCertificate(@PathVariable Long courseId,
                                                      @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        if (customUserDetail == null) {
            return ResponseEntity.badRequest().body(null);
        }

        Long userId = customUserDetail.getId();

        // Kiểm tra xem người dùng đã hoàn thành tất cả bài học chưa
        List<Lesson> lessons = courseService.getCourseById(courseId).getLessons();
        List<Progress> progressList = progressService.getUserProgressInCourse(userId, courseId);

        boolean allCompleted = lessons.stream()
                .allMatch(lesson -> progressList.stream()
                        .anyMatch(progress -> progress.getLesson() != null // Kiểm tra Lesson có null hay không
                                && Long.valueOf(progress.getLesson().getId()).equals(lesson.getId()) // So sánh ID an toàn
                                && progress.isCompleted())); // Kiểm tra trạng thái hoàn thành

        if (!allCompleted) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // Lấy thông tin người dùng và chữ ký
            User user = userService.findByUsername(customUserDetail.getUsername());

            // Tạo chứng chỉ
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            // Thiết kế chứng chỉ
            Font titleFont = new Font(Font.HELVETICA, 36, Font.BOLD, Color.BLUE);
            Font contentFont = new Font(Font.HELVETICA, 18, Font.NORMAL, Color.BLACK);
            Font smallFont = new Font(Font.HELVETICA, 14, Font.ITALIC, Color.GRAY);
            Font signatureFont = new Font(Font.COURIER, 20, Font.ITALIC, Color.DARK_GRAY); // Font viết tay

            // Tiêu đề chứng chỉ
            Paragraph title = new Paragraph("GIẤY CHỨNG NHẬN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\n\n"));

            // Tên khóa học
            Paragraph courseName = new Paragraph(
                    "Khóa học: " + courseService.getCourseById(courseId).getName(),
                    new Font(Font.HELVETICA, 24, Font.BOLD, Color.DARK_GRAY));
            courseName.setAlignment(Element.ALIGN_CENTER);
            document.add(courseName);

            document.add(new Paragraph("\n"));

            // Thông tin người dùng
            Paragraph userInfo = new Paragraph(
                    "Được cấp cho: " + customUserDetail.getUsername(),
                    contentFont);
            userInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(userInfo);

            document.add(new Paragraph("\n"));

            // Email, Số điện thoại, Địa chỉ
            Paragraph email = new Paragraph("Email: " + user.getEmail(), smallFont);
            email.setAlignment(Element.ALIGN_CENTER);
            document.add(email);

            Paragraph phone = new Paragraph("Số điện thoại: " + user.getPhoneNumber(), smallFont);
            phone.setAlignment(Element.ALIGN_CENTER);
            document.add(phone);

            Paragraph address = new Paragraph("Địa chỉ: " + user.getAddress(), smallFont);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);

            document.add(new Paragraph("\n\n"));

            // Ngày cấp
            Paragraph issueDate = new Paragraph(
                    "Ngày cấp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    contentFont);
            issueDate.setAlignment(Element.ALIGN_CENTER);
            document.add(issueDate);

            document.add(new Paragraph("\n\n\n"));

            // Lời cảm ơn
            Paragraph thankYou = new Paragraph(
                    "Cảm ơn bạn đã tham gia khóa học và chúc bạn thành công trong hành trình học tập!",
                    smallFont);
            thankYou.setAlignment(Element.ALIGN_CENTER);
            document.add(thankYou);

            // Thêm khoảng cách trước phần chữ ký
            document.add(new Paragraph("\n\n"));

            // Tạo đoạn "Chữ ký" với font nhỏ và căn giữa
            Paragraph signatureTitle = new Paragraph("Chữ ký", contentFont); // Font nhỏ gọn, đẹp
            signatureTitle.setAlignment(Element.ALIGN_CENTER); // Căn giữa chữ "Chữ ký"

            // Tạo đoạn tên người dùng với font nghệ thuật
            Paragraph userSignature = new Paragraph(user.getName(), signatureFont); // Font kiểu chữ ký
            userSignature.setAlignment(Element.ALIGN_CENTER); // Căn giữa tên người dùng

            // Thêm cả hai đoạn vào tài liệu
            document.add(signatureTitle); // Thêm "Chữ ký" vào
            document.add(new Paragraph("\n")); // Thêm một khoảng trống nhỏ
            document.add(userSignature); // Thêm tên người dùng dưới "Chữ ký"

            // Viền chứng chỉ
            Rectangle border = new Rectangle(document.getPageSize());
            border.setBorder(Rectangle.BOX);
            border.setBorderWidth(5);
            border.setBorderColor(Color.BLUE);
            document.add(border);

            document.close();

            // Trả về file PDF với đúng header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.add("Content-Disposition", "attachment; filename=certificate.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
