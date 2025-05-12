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
    public ScheduleResponse createSchedule(AuthUser authUser, ScheduleCreateRequest scheduleCreateRequest) {
        Company company = companyRepository.findById(scheduleCreateRequest.getCompanyId()).orElseThrow(CompanyNotFoundException::new);

        company.validateOwner(authUser.getId());

        User user = userRepository.findById(authUser.getId()).orElseThrow(UserNotFoundException::new);

        Schedule schedule = Schedule.builder()
                .user(user)
                .company(company)
                .dueDate(scheduleCreateRequest.getDueDate())
                .step(scheduleCreateRequest.getStep())
                .position(scheduleCreateRequest.getPosition())
                .memo(scheduleCreateRequest.getMemo())
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);

        return ScheduleResponse.from(savedSchedule);
    }

    @Transactional
    public void updateSchedule(AuthUser authUser, Long scheduleId, ScheduleUpdateRequest scheduleUpdateRequest) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(ScheduleNotFoundException::new);

        schedule.validateOwner(authUser.getId());

        schedule.update(
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
}
