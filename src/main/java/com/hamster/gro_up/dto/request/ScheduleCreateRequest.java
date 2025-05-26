package com.hamster.gro_up.dto.request;

import com.hamster.gro_up.entity.Step;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ScheduleCreateRequest {

    private Long companyId;

    @NotBlank
    private String companyName;

    private String companyLocation;

    private Step step;

    @NotNull
    private LocalDateTime dueDate;

    private String position;

    private String memo;
}
