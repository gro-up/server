package com.hamster.gro_up.advise;

import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.exception.BadRequestException;
import com.hamster.gro_up.exception.ForbiddenException;
import com.hamster.gro_up.exception.NotFoundException;
import com.hamster.gro_up.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ApiResponse<Object> handleBadRequestException(BadRequestException e) {
        log.error(e.getMessage());
        
        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                null
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<Object> handleAuthenticationException(UnauthorizedException e) {
        log.error(e.getMessage());
        
        return ApiResponse.of(
                HttpStatus.UNAUTHORIZED,
                e.getMessage(),
                null
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ApiResponse<Object> handleForbiddenException(ForbiddenException e) {
        log.error(e.getMessage());

        return ApiResponse.of(
                HttpStatus.FORBIDDEN,
                e.getMessage(),
                null
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ApiResponse<Object> handleNotFoundException(NotFoundException e) {
        log.error(e.getMessage());

        return ApiResponse.of(
                HttpStatus.NOT_FOUND,
                e.getMessage(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleException(Exception e) {
        log.error("Unhandled exception", e);

        return ApiResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 오류가 발생했습니다.",
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Object> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

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

        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                sb.toString(),
                null
        );
    }
}
