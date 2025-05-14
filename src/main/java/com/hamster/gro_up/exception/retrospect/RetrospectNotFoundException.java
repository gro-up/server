package com.hamster.gro_up.exception.retrospect;

import com.hamster.gro_up.exception.NotFoundException;

public class RetrospectNotFoundException extends NotFoundException {
    private static final String MESSAGE = "해당 회고를 찾을 수 없습니다.";

    public RetrospectNotFoundException() {super(MESSAGE);}

    public RetrospectNotFoundException(String message) {super(message);}
}
