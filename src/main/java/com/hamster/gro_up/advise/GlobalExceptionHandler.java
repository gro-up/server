package com.hamster.gro_up.advise;

import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(BadRequestException e) {
        log.error(e.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(ApiResponse.of(status, e.getMessage(), null));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(UnauthorizedException e) {
        log.error(e.getMessage());

        HttpStatus status = HttpStatus.UNAUTHORIZED;

        return ResponseEntity.status(status).body(ApiResponse.of(status, e.getMessage(), null));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Object>> handleForbiddenException(ForbiddenException e) {
        log.error(e.getMessage());

        HttpStatus status = HttpStatus.FORBIDDEN;

        return ResponseEntity.status(status).body(ApiResponse.of(status, e.getMessage(), null));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(NotFoundException e) {
        log.error(e.getMessage());

        HttpStatus status = HttpStatus.NOT_FOUND;

        return ResponseEntity.status(status).body(ApiResponse.of(status, e.getMessage(), null));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflictException(ConflictException e) {
        log.error(e.getMessage());

        HttpStatus status = HttpStatus.CONFLICT; // 409

        return ResponseEntity.status(status).body(ApiResponse.of(status, e.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("Unhandled exception", e);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(status).body(ApiResponse.of(status, e.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());

        BindingResult bindingResult = e.getBindingResult();

        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<String> errorMessages = bindingResult.getFieldErrors().stream()
                .map(fieldError -> String.format("[%s](은)는 %s 입력된 값: [%s]",
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()))
                .toList();
        return ResponseEntity.status(status).body(ApiResponse.of(status, errorMessages, null));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParam(MissingServletRequestParameterException e) {
        log.error(e.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<String> errorMessages = List.of(e.getMessage());

        return ResponseEntity.status(status).body(ApiResponse.of(status, errorMessages, null));
    }
}
