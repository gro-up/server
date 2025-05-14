package com.hamster.gro_up.dto.response;

import com.hamster.gro_up.entity.Company;
import com.hamster.gro_up.entity.Retrospect;
import com.hamster.gro_up.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class RetrospectResponse {

    private Long scheduleId;

    private String memo;

    private Long companyId;

    private String companyName;

    private String position;

    private LocalDateTime createdAt;

    public static RetrospectResponse from(Retrospect retrospect) {
        Schedule schedule = retrospect.getSchedule();
        Company company = schedule.getCompany();

        return new RetrospectResponse(
                retrospect.getSchedule().getId(),
                retrospect.getMemo(),
                company != null ? company.getId() : null,
                retrospect.getSchedule().getCompanyName(),
                retrospect.getSchedule().getPosition(),
                retrospect.getCreatedAt()
        );
    }
}
