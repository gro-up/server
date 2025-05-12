package com.hamster.gro_up.exception.schedule;

import com.hamster.gro_up.exception.NotFoundException;

public class ScheduleNotFoundException extends NotFoundException {
    private static final String MESSAGE = "해당 일정을 찾을 수 없습니다.";

    public ScheduleNotFoundException() {super(MESSAGE);}

    public ScheduleNotFoundException(String message) {super(message);}
}
