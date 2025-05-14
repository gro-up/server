package com.hamster.gro_up.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RetrospectCreateRequest {

    @NotNull
    private Long scheduleId;

    @NotBlank
    private String memo;
}
