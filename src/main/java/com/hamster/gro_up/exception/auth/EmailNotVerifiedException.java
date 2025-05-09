package com.hamster.gro_up.exception.auth;

import com.hamster.gro_up.exception.ForbiddenException;

public class EmailNotVerifiedException extends ForbiddenException {
    private static final String MESSAGE = "인증되지 않은 이메일입니다.";

    public EmailNotVerifiedException() {super(MESSAGE);}

    public EmailNotVerifiedException(String message) {super(message);}
}
