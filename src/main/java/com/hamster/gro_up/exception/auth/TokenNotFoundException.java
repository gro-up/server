package com.hamster.gro_up.exception.auth;

import com.hamster.gro_up.exception.UnauthorizedException;

public class TokenNotFoundException extends UnauthorizedException {
    private static final String MESSAGE = "토큰을 찾을 수 없습니다.";

    public TokenNotFoundException() {super(MESSAGE);}

    public TokenNotFoundException(String message) {super(message);}
}
