package com.hamster.gro_up.dto.response;

import com.hamster.gro_up.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ScheduleResponse {

    private Long scheduleId;

    private Long companyId;

    private String companyName;

    private String address;

    private String addressDetail;

    private String step;

    private String position;

    private String memo;

    private LocalDateTime dueDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static ScheduleResponse from(Schedule schedule) {
        Long companyId = (schedule.getCompany() != null) ? schedule.getCompany().getId() : null;

        return new ScheduleResponse(
                schedule.getId(),
                companyId,
                schedule.getCompanyName(),
                schedule.getAddress(),
                schedule.getAddressDetail(),
                schedule.getStep().getDisplayName(),
                schedule.getPosition(),
                schedule.getMemo(),
                schedule.getDueDate(),
                schedule.getCreatedAt(),
                schedule.getModifiedAt());
    }
}
