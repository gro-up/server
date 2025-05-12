package com.hamster.gro_up.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ScheduleListResponse {
    List<ScheduleResponse> scheduleList;

    public static ScheduleListResponse of(List<ScheduleResponse> scheduleList) {
        return new ScheduleListResponse(scheduleList);
    }
}
