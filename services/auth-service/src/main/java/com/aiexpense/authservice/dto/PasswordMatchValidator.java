package com.aiexpense.authservice.dto;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchValidator.class)
@Documented
@interface PasswordMatch {
    String message() default "Passwords do not match";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String password() default "password";

    String confirmPassword() default "confirmPassword";
}


class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, RegisterRequest> {

    private String passwordFieldName;
    private String confirmPasswordFieldName;

    @Override
    public void initialize(PasswordMatch annotation) {
        this.passwordFieldName = annotation.password();
        this.confirmPasswordFieldName = annotation.confirmPassword();
    }

    @Override
    public boolean isValid(RegisterRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String password = value.getPassword();
        String confirmPassword = value.getConfirmPassword();

        if (password == null || confirmPassword == null) {
            return true;
        }

        boolean isValid = password.equals(confirmPassword);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Passwords do not match")
                    .addPropertyNode(confirmPasswordFieldName)
                    .addConstraintViolation();
        }

        return isValid;
    }
}
