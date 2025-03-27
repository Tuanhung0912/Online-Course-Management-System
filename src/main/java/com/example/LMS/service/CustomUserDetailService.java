package com.example.LMS.service;

import com.example.LMS.model.CustomUserDetail;
import com.example.LMS.model.User;
import com.example.LMS.repository.RoleRepository;
import com.example.LMS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomUserDetailService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Inject PasswordEncoder

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new CustomUserDetail(user, userRepository);
    }

    public UserDetails loadUserByOAuth2Google(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        User user = userRepository.findByUsername(email);
        if (user == null) {
            // Nếu không tìm thấy, tạo người dùng mới từ OAuth2
            user = new User();
            user.setUsername(email);
            user.setName((String) attributes.get("name"));
            user.setEmail(email);
            user.setImageData((String) attributes.get("picture")); // Lưu ảnh Google
            user.setPassword(passwordEncoder.encode("OAUTH2_DEFAULT_PASSWORD"));

            userService.save(user);
        }

        return new CustomUserDetail(user, userRepository, attributes);
    }

}
