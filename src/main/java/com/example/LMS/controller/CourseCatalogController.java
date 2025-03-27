package com.example.LMS.controller;

import com.example.LMS.model.CourseCatalog;
import com.example.LMS.service.CourseCatalogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/courseCatalogs")
public class CourseCatalogController {

    @Autowired
    private CourseCatalogService courseCatalogService;

    // Hiện thị danh sách danh mục
    @GetMapping
    public String showAllCourseCatalogs(Model model) {
        List<CourseCatalog> courseCatalogs = courseCatalogService.getAllCourseCatalog();
        model.addAttribute("courseCatalogs", courseCatalogs);
        return "courseCatalog/list";
    }


    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showAddForm(Model model) {
        model.addAttribute("courseCatalog", new CourseCatalog());
        return "courseCatalog/add";
    }

    // Thực hiện chức năng thêm một danh mục
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String addCourseCatalog(@Valid @ModelAttribute("courseCatalog") CourseCatalog courseCatalog, BindingResult result) {
        if (result.hasErrors()) {
            return "courseCatalog/add";
        }
        courseCatalogService.saveCourseCatalog(courseCatalog);
        return "redirect:/courseCatalogs";
    }


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        CourseCatalog courseCatalog = courseCatalogService.getCourseCatalogById(id);
        if (courseCatalog != null) {
            model.addAttribute("courseCatalog", courseCatalog);
            return "courseCatalog/edit";
        }
        return "redirect:/courseCatalogs";
    }

    // Thực hiện chức năng cập nhật danh mục
    @PostMapping("/edit/{id}")
    public String updateCourseCatalog(@PathVariable("id") Long id, @Valid @ModelAttribute("courseCatalog") CourseCatalog courseCatalog, BindingResult result) {
        if (result.hasErrors()) {
            return "courseCatalog/edit";
        }
        courseCatalogService.saveCourseCatalog(courseCatalog);
        return "redirect:/courseCatalogs";
    }

    // Thực hiện chức năng xóa danh mục
    @GetMapping("/delete/{id}")
    public String deleteCourseCatalog(@PathVariable("id") Long id) {
        courseCatalogService.deleteCourseCatalog(id);
        return "redirect:/courseCatalogs";
    }
}
