package com.example.LMS.validator;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidCourseCatalogIdValidator.class)
public @interface ValidCourseCatalogId {
    String message() default "Danh mục khóa học không hợp lệ";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
