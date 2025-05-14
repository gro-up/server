package com.hamster.gro_up.dto.request;

import com.hamster.gro_up.entity.Step;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ScheduleCreateRequest {

    private Long companyId;

    @NotBlank
    private String companyName;

    private Step step;

    private LocalDateTime dueDate;

    private String position;

    private String memo;
}
