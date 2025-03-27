package com.example.LMS.controller;

import com.example.LMS.model.CustomUserDetail;
import com.example.LMS.model.User;
import com.example.LMS.service.CustomUserDetailService;
import com.example.LMS.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                model.addAttribute(error.getField() + "_error",
                        error.getDefaultMessage());
            }
            return "user/register";
        }

        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userService.save(user);
        return "redirect:/login";
    }

    // Hàm lưu file ảnh
    private String saveAvatarStatic(MultipartFile image) throws IOException {
        File saveFile = new ClassPathResource("static/avatar").getFile();
        String fileName = UUID.randomUUID()+ "." + StringUtils.getFilenameExtension(image.getOriginalFilename());
        Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
        Files.copy(image.getInputStream(), path);

        // Trả về file hoàn chỉnh
        return fileName;
    }

    // Hiển thị thông tin chi tiết của user
    @GetMapping("/user/{id}")
    public String viewUserDetails(@PathVariable("id") Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "user/view"; // Tạo view.html để hiển thị thông tin chi tiết người dùng
    }

    // Hiển thị form chỉnh sửa thông tin user
    @GetMapping("/user/edit/{id}")
    public String editUser(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        User user = userService.findById(id);
        model.addAttribute("user", user);

        // Kiểm tra nếu người dùng đăng nhập bằng OAuth2
        boolean isOAuth2User = customUserDetail.getAttributes() != null && !customUserDetail.getAttributes().isEmpty();

        model.addAttribute("isOAuth2User", isOAuth2User);
        return "user/edit"; // Tạo edit.html để chứa form chỉnh sửa thông tin người dùng
    }

    // Thực hiện chức năng sửa thông tin khóa
    @PostMapping("/user/edit/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("user") User user,
                             @RequestParam("image") MultipartFile imageFile,
                             BindingResult bindingResult, Model model,
                             @AuthenticationPrincipal CustomUserDetail customUserDetail) {

        // Nếu kết quả trả về lỗi
        if (bindingResult.hasErrors()) {

            // Lấy danh sách lỗi
            List<FieldError> errors = bindingResult.getFieldErrors();

            // Lặp qua từng test case của từng lỗi
            for (FieldError error : errors) {
                model.addAttribute(error.getField() + "_error",
                        error.getDefaultMessage());
            }
            return "user/edit"; // Nếu có lỗi, trả lại trang chỉnh sửa
        }


        User existingUser = userService.findById(id);
        if (existingUser != null) {
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setUsername(user.getUsername());
            existingUser.setPassword(user.getPassword());
            existingUser.setAddress(user.getAddress());
            existingUser.setPhoneNumber(user.getPhoneNumber());

            // Kiểm tra xem người dùng có muốn đổi mật khẩu hay không
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                // Mã hóa mật khẩu
                existingUser.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
            }


            /*if (isOAuth2User) {
                // Người dùng OAuth2 chỉ có thể chỉnh sửa số điện thoại, địa chỉ và ảnh đại diện
                existingUser.setPhoneNumber(user.getPhoneNumber());
                existingUser.setAddress(user.getAddress());
            } else {
                // Người dùng thông thường có thể chỉnh sửa tất cả các trường
                existingUser.setName(user.getName());
                existingUser.setEmail(user.getEmail());
                existingUser.setUsername(user.getUsername());
                existingUser.setPassword(user.getPassword());

                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    existingUser.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
                }
            }*/

            // Xử lý lưu file ảnh
            if (!imageFile.isEmpty()) {
                try {
                    String imageName = saveAvatarStatic(imageFile);
                    existingUser.setImageData("/avatar/" +imageName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            userService.saveTT(existingUser); // Lưu thông tin đã chỉnh sửa vào cơ sở dữ liệu
        }

        return "redirect:/user/" + id; // Sau khi cập nhật, điều hướng về trang chi tiết user
    }

    // Endpoint cung cấp ảnh đại diện
    /*@GetMapping("/user/avatar/{id}")
    public ResponseEntity<byte[]> getUserAvatar(@PathVariable("id") Long id) {
        User user = userService.findById(id);
        try {
            if (user != null && user.getImageData() != null) {
                Path imagePath = Paths.get(new ClassPathResource("static" + user.getImageData()).getFile().getAbsolutePath());
                byte[] imageBytes = Files.readAllBytes(imagePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(imageBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Trả về ảnh mặc định nếu không có ảnh đại diện
        try {
            Path defaultImagePath = Paths.get(new ClassPathResource("static/images/avatar.png").getFile().getAbsolutePath());
            byte[] defaultImageBytes = Files.readAllBytes(defaultImagePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(defaultImageBytes);
        } catch (IOException e) {
            throw new RuntimeException("Error loading default image", e);
        }
    }*/

    private byte[] fetchImageFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();

            try (InputStream inputStream = connection.getInputStream()) {
                return inputStream.readAllBytes();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error fetching image from URL: " + imageUrl, e);
        }
    }


    @GetMapping("/user/avatar/{id}")
    public ResponseEntity<byte[]> getUserAvatar(@PathVariable("id") Long id) {
        User user = userService.findById(id);

        try {
            if (user != null && user.getImageData() != null) {
                // Trường hợp ảnh từ URL (Google OAuth2)
                if (user.getImageData().startsWith("http")) {
                    byte[] imageBytes = fetchImageFromUrl(user.getImageData());
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .body(imageBytes);
                }

                // Trường hợp ảnh lưu trên server
                Path imagePath = Paths.get(new ClassPathResource("static/avatar" + user.getImageData()).getFile().getAbsolutePath());
                byte[] imageBytes = Files.readAllBytes(imagePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(imageBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Trả về ảnh mặc định nếu không có ảnh đại diện
        try {
            Path defaultImagePath = Paths.get(new ClassPathResource("static/avatar/avatar.png").getFile().getAbsolutePath());
            byte[] defaultImageBytes = Files.readAllBytes(defaultImagePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(defaultImageBytes);
        } catch (IOException e) {
            throw new RuntimeException("Error loading default image", e);
        }
    }

}

