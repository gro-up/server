package com.hamster.gro_up.exception.company;

import com.hamster.gro_up.exception.NotFoundException;

public class CompanyNotFoundException extends NotFoundException {
    private static final String MESSAGE = "해당 기업을 찾을 수 없습니다.";

    public CompanyNotFoundException() {super(MESSAGE);}

    public CompanyNotFoundException(String message) {super(message);}
}
