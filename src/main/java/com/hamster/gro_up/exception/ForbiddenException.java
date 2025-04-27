package com.hamster.gro_up.exception;

public class ForbiddenException extends RuntimeException{
    private static final String MESSAGE = "접근이 금지되었습니다.";

    public ForbiddenException() {super(MESSAGE);}

    public ForbiddenException(String message) {super(message);}
}
