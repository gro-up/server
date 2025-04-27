package com.hamster.gro_up.exception;

public class NotFoundException extends RuntimeException {
    private static final String MESSAGE = "리소스를 찾을 수 없습니다.";

    public NotFoundException() {super(MESSAGE);}

    public NotFoundException(String message) {super(message);}
}
