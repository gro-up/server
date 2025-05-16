package com.hamster.gro_up.exception.auth;

import com.hamster.gro_up.exception.UnauthorizedException;

public class ExpiredTokenException extends UnauthorizedException {
    private static final String MESSAGE = "만료된 토큰입니다.";

    public ExpiredTokenException() {super(MESSAGE);}

    public ExpiredTokenException(String message) {super(message);}
}
