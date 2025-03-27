package com.example.LMS.controller;

import com.example.LMS.model.CourseCatalog;
import com.example.LMS.service.CourseCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CourseCatalogService courseCatalogService;

    // Cung cấp danh mục khóa học cho tất cả các trang
    @ModelAttribute("courseCatalogs")
    public List<CourseCatalog> getCourseCatalogs() {
        return courseCatalogService.getAllCourseCatalog();
    }
}
