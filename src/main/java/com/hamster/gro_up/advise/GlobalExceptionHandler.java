package com.hamster.gro_up.advise;

import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.exception.BadRequestException;
import com.hamster.gro_up.exception.ForbiddenException;
import com.hamster.gro_up.exception.NotFoundException;
import com.hamster.gro_up.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append("[");
            sb.append(fieldError.getField());
            sb.append("](은)는 ");
            sb.append(fieldError.getDefaultMessage());
            sb.append(" 입력된 값: [");
            sb.append(fieldError.getRejectedValue());
            sb.append("]");
        }

        return ResponseEntity.status(status).body(ApiResponse.of(status, sb.toString(), null));
    }
}
