package com.hamster.gro_up.exception.auth;

import com.hamster.gro_up.exception.BadRequestException;

public class InvalidEmailVerificationTokenException extends BadRequestException {
    private static final String MESSAGE = "인증 코드가 유효하지 않습니다.";

    public InvalidEmailVerificationTokenException() {super(MESSAGE);}

    public InvalidEmailVerificationTokenException(String message) {super(message);}
}
