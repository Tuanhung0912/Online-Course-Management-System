package com.example.LMS.model;

import com.example.LMS.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;


public class CustomUserDetail implements UserDetails {
    private final User user;
    private final UserRepository userRepository;
    private final Map<String, Object> attributes;

    // Constructor với OAuth2 attributes
    public CustomUserDetail(User user, UserRepository userRepository, Map<String, Object> attributes) {
        this.user = user;
        this.userRepository = userRepository;
        this.attributes = attributes != null ? attributes : Collections.emptyMap(); // Gán giá trị mặc định
    }

    // Constructor không có OAuth2 attributes
    public CustomUserDetail(User user, UserRepository userRepository) {
        this(user, userRepository, Collections.emptyMap()); // Gọi constructor chính
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities () {
        return Arrays.stream(userRepository.getRoleOfUser(user.getId()))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }


    public Long getId() {
        return user.getId();
    }

    public String getImageData() {
        // Ưu tiên lấy ảnh từ OAuth2 nếu có
        if (attributes != null && attributes.containsKey("picture")) {
            return (String) attributes.get("picture"); // Ảnh từ Google OAuth2
        }
        // Nếu không, trả về ảnh từ User thông thường
        return user.getImageData();
    }

    public Map<String, Object> getAttributes() {
        return attributes; // Trả về các thuộc tính OAuth2
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
