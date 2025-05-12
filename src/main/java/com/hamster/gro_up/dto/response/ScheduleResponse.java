package com.hamster.gro_up.dto.response;

import com.hamster.gro_up.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ScheduleResponse {

    private Long companyId;

    private String companyName;

    private String step;

    private String position;

    private String memo;

    private LocalDateTime dueDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(
                schedule.getCompany().getId(),
                schedule.getCompany().getCompanyName(),
                schedule.getStep().getDisplayName(),
                schedule.getPosition(),
                schedule.getMemo(),
                schedule.getDueDate(),
                schedule.getCreatedAt(),
                schedule.getModifiedAt());
    }
}
