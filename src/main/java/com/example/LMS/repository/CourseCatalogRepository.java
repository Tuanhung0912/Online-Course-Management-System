package com.example.LMS.repository;

import com.example.LMS.model.CourseCatalog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface CourseCatalogRepository extends JpaRepository<CourseCatalog, Long> {

    @Query("SELECT cc FROM CourseCatalog cc LEFT JOIN cc.courses c GROUP BY cc.id ORDER BY COUNT(c.id) DESC")
    List<CourseCatalog> findTopCourseCatalogsWithMostCourses(Pageable pageable);
}

