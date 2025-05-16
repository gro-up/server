package com.hamster.gro_up.exception.auth;

import com.hamster.gro_up.exception.UnauthorizedException;

public class InvalidTokenException extends UnauthorizedException {
    private static final String MESSAGE = "올바르지 않은 토큰입니다.";

    public InvalidTokenException() {super(MESSAGE);}

    public InvalidTokenException(String message) {super(message);}
}
