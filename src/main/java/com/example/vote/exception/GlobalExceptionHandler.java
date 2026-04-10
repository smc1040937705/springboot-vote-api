package com.example.vote.exception;

import com.example.vote.common.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.StringJoiner;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        StringJoiner sj = new StringJoiner(", ");
        e.getBindingResult().getFieldErrors().forEach(error ->
                sj.add(error.getField() + ": " + error.getDefaultMessage())
        );
        return Result.error(400, sj.toString());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        StringJoiner sj = new StringJoiner(", ");
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            sj.add(violation.getMessage());
        }
        return Result.error(400, sj.toString());
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        StringJoiner sj = new StringJoiner(", ");
        e.getBindingResult().getFieldErrors().forEach(error ->
                sj.add(error.getField() + ": " + error.getDefaultMessage())
        );
        return Result.error(400, sj.toString());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        return Result.error(500, "服务器内部错误: " + e.getMessage());
    }
}
