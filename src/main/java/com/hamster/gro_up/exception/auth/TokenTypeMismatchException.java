package com.hamster.gro_up.exception.auth;

import com.hamster.gro_up.exception.UnauthorizedException;

public class TokenTypeMismatchException extends UnauthorizedException {
    private static final String MESSAGE = "Token type 이 일치하지 않습니다.";

    public TokenTypeMismatchException() {super(MESSAGE);}

    public TokenTypeMismatchException(String message) {super(message);}
}
