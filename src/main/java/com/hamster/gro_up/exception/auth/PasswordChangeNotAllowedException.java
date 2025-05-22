package com.hamster.gro_up.exception.auth;

import com.hamster.gro_up.exception.ForbiddenException;

public class PasswordChangeNotAllowedException extends ForbiddenException {
    private static final String MESSAGE = "비밀번호 변경이 허용되지 않는 사용자입니다.";

    public PasswordChangeNotAllowedException() {super(MESSAGE);}

    public PasswordChangeNotAllowedException(String message) {super(message);}
}
