package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.ScheduleCreateRequest;
import com.hamster.gro_up.dto.request.ScheduleUpdateRequest;
import com.hamster.gro_up.dto.response.ScheduleListResponse;
import com.hamster.gro_up.dto.response.ScheduleResponse;
import com.hamster.gro_up.entity.Company;
import com.hamster.gro_up.entity.Schedule;
import com.hamster.gro_up.entity.User;
import com.hamster.gro_up.exception.company.CompanyNotFoundException;
import com.hamster.gro_up.exception.schedule.ScheduleNotFoundException;
import com.hamster.gro_up.exception.user.UserNotFoundException;
import com.hamster.gro_up.repository.CompanyRepository;
import com.hamster.gro_up.repository.ScheduleRepository;
import com.hamster.gro_up.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public ScheduleResponse findSchedule(AuthUser authUser, Long scheduleId) {
        Schedule schedule = scheduleRepository.findByIdWithCompany(scheduleId).orElseThrow(ScheduleNotFoundException::new);

        schedule.validateOwner(authUser.getId());

        return ScheduleResponse.from(schedule);
    }

    public ScheduleListResponse findAllSchedules(AuthUser authUser) {
        List<Schedule> scheduleList = scheduleRepository.findByUserIdWithCompany(authUser.getId());

        List<ScheduleResponse> responseList = scheduleList.stream().map(ScheduleResponse::from).toList();

        return ScheduleListResponse.of(responseList);
    }

    @Transactional
    public ScheduleResponse createSchedule(AuthUser authUser, ScheduleCreateRequest request) {
        Company company = null;
        String companyName;
        String companyLocation;

        if (request.getCompanyId() != null) {
            company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(CompanyNotFoundException::new);

            company.validateOwner(authUser.getId());

            companyName = company.getCompanyName();
            companyLocation = company.getLocation();
        } else {
            companyName = request.getCompanyName();
            companyLocation = request.getCompanyLocation();
        }

        User user = userRepository.findById(authUser.getId()).orElseThrow(UserNotFoundException::new);

        Schedule schedule = Schedule.builder()
                .user(user)
                .company(company)
                .companyName(companyName)
                .companyLocation(companyLocation)
                .dueDate(request.getDueDate())
                .step(request.getStep())
                .position(request.getPosition())
                .memo(request.getMemo())
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);

        return ScheduleResponse.from(savedSchedule);
    }

    @Transactional
    public void updateSchedule(AuthUser authUser, Long scheduleId, ScheduleUpdateRequest scheduleUpdateRequest) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(ScheduleNotFoundException::new);

        schedule.validateOwner(authUser.getId());

        Company company = null;

        Long companyId = scheduleUpdateRequest.getCompanyId();
        String companyName = scheduleUpdateRequest.getCompanyName();
        String companyLocation = scheduleUpdateRequest.getCompanyLocation();

        if (companyId != null) {
            company = companyRepository.findById(companyId).orElseThrow(CompanyNotFoundException::new);

            company.validateOwner(authUser.getId());

            companyName = company.getCompanyName();
            companyLocation = company.getLocation();
        }

        schedule.update(
                company,
                companyName,
                companyLocation,
                scheduleUpdateRequest.getDueDate(),
                scheduleUpdateRequest.getMemo(),
                scheduleUpdateRequest.getPosition(),
                scheduleUpdateRequest.getStep()
        );
    }

    @Transactional
    public void deleteSchedule(AuthUser authUser, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(ScheduleNotFoundException::new);

        schedule.validateOwner(authUser.getId());

        scheduleRepository.delete(schedule);
    }

    public ScheduleListResponse findSchedulesInRange(AuthUser authUser, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Schedule> schedules = scheduleRepository.findSchedulesInRange(authUser.getId(), startDateTime, endDateTime);

        List<ScheduleResponse> responseList = schedules.stream().map(ScheduleResponse::from).toList();

        return ScheduleListResponse.of(responseList);
    }

    public ScheduleListResponse findSchedulesByCompanyName(AuthUser authUser, String companyName) {
        List<Schedule> schedules = scheduleRepository.findByUserIdAndCompanyName(authUser.getId(), companyName);

        List<ScheduleResponse> responseList = schedules.stream().map(ScheduleResponse::from).toList();

        return ScheduleListResponse.of(responseList);
    }
}
