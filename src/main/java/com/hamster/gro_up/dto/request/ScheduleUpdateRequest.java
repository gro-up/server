package com.hamster.gro_up.dto.request;

import com.hamster.gro_up.entity.Step;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ScheduleUpdateRequest {

    private Step step;

    private LocalDateTime dueDate;

    private String position;

    private String memo;
}
