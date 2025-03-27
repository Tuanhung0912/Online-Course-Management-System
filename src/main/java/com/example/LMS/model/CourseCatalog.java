package com.example.LMS.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "courseCatalog")
public class CourseCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Danh mục không được để trống")
    @Size(max = 50, message = "Tên danh mục phải ít hơn 50 ký tự")
    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "courseCatalog", cascade = CascadeType.ALL)
    private List<Course> courses;

}
