package com.example.LMS.validator;

import com.example.LMS.model.CourseCatalog;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidCourseCatalogIdValidator implements ConstraintValidator<ValidCourseCatalogId, CourseCatalog> {
    @Override
    public boolean isValid(CourseCatalog courseCatalog, ConstraintValidatorContext constraintValidatorContext) {
        return courseCatalog != null && courseCatalog.getId() != null;
    }
}
