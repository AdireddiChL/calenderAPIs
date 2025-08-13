package org.assignment.config;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.assignment.exceptions.BadRequestException;
import org.assignment.exceptions.ConflictException;
import org.assignment.exceptions.InternalServerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getClass().getSimpleName());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), ex.getClass().getSimpleName());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        return build(HttpStatus.BAD_REQUEST, "Validation failed", BadRequestException.class.getSimpleName());
    }

    // Validate JSON format issues (e.g. LocalDate/LocalTime patterns) and return clear messages
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            Class<?> targetType = ife.getTargetType();
            String field = (ife.getPath() != null && !ife.getPath().isEmpty()) ? ife.getPath().get(0).getFieldName() : "";
            if (targetType == LocalDate.class) {
                String msg = field.isEmpty() ? "Invalid date format, expected yyyy-MM-dd" :
                        String.format("Invalid format for '%s', expected yyyy-MM-dd", field);
                return build(HttpStatus.BAD_REQUEST, msg, BadRequestException.class.getSimpleName());
            }
            if (targetType == LocalTime.class) {
                String msg = field.isEmpty() ? "Invalid time format, expected HH:mm" :
                        String.format("Invalid format for '%s', expected HH:mm", field);
                return build(HttpStatus.BAD_REQUEST, msg, BadRequestException.class.getSimpleName());
            }
        }
        return build(HttpStatus.BAD_REQUEST, "Incorrect" +
                " request body", BadRequestException.class.getSimpleName());
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<Map<String, Object>> handleInternal(InternalServerException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getClass().getSimpleName());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOthers(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getClass().getSimpleName());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, String type) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("type", type);
        return ResponseEntity.status(status).body(body);
    }
}


