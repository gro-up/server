package com.hamster.gro_up.exception.auth;

import com.hamster.gro_up.exception.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {
    private static final String MESSAGE = "이메일 또는 비밀번호가 일치하지 않습니다.";

    public InvalidCredentialsException() {super(MESSAGE);}

    public InvalidCredentialsException(String message) {super(message);}
}
