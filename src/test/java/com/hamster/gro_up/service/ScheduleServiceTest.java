package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.ScheduleCreateRequest;
import com.hamster.gro_up.dto.request.ScheduleUpdateRequest;
import com.hamster.gro_up.dto.response.ScheduleListResponse;
import com.hamster.gro_up.dto.response.ScheduleResponse;
import com.hamster.gro_up.entity.*;
import com.hamster.gro_up.exception.ForbiddenException;
import com.hamster.gro_up.exception.schedule.ScheduleNotFoundException;
import com.hamster.gro_up.repository.CompanyRepository;
import com.hamster.gro_up.repository.ScheduleRepository;
import com.hamster.gro_up.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private User user;
    private Company company;
    private Schedule schedule;
    private AuthUser authUser;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .build();

        company = Company.builder()
                .id(10L)
                .user(user)
                .companyName("ham-corp")
                .position("back-end")
                .location("seoul")
                .url("www.ham.com")
                .build();

        schedule = Schedule.builder()
                .id(100L)
                .user(user)
                .company(company)
                .companyName(company.getCompanyName())
                .dueDate(LocalDateTime.of(2025, 5, 11, 12, 0))
                .step(Step.DOCUMENT)
                .position("백엔드")
                .memo("메모입니다")
                .build();

        authUser = new AuthUser(1L, "ham@gmail.com", Role.ROLE_USER);
    }

    @Test
    @DisplayName("일정 단건 조회에 성공한다")
    void findSchedule_success() {
        // given
        given(scheduleRepository.findByIdWithCompany(schedule.getId())).willReturn(Optional.of(schedule));

        // when
        ScheduleResponse response = scheduleService.findSchedule(authUser, schedule.getId());

        // then
        assertThat(response.getCompanyId()).isEqualTo(company.getId());
        assertThat(response.getCompanyName()).isEqualTo(company.getCompanyName());
        assertThat(response.getStep()).isEqualTo(schedule.getStep().getDisplayName());
        assertThat(response.getPosition()).isEqualTo(schedule.getPosition());
        assertThat(response.getMemo()).isEqualTo(schedule.getMemo());
        assertThat(response.getDueDate()).isEqualTo(schedule.getDueDate());
    }

    @Test
    @DisplayName("존재하지 않는 일정 조회 시 예외가 발생한다")
    void findSchedule_fail_notExist() {
        // given
        given(scheduleRepository.findByIdWithCompany(999L)).willReturn(Optional.empty());

        // when & then
        ScheduleNotFoundException exception = assertThrows(ScheduleNotFoundException.class,
                () -> scheduleService.findSchedule(authUser, 999L));
        assertThat(exception.getMessage()).isEqualTo("해당 일정을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("일정 조회 시 소유자가 아니면 예외가 발생한다")
    void findSchedule_fail_notOwner() {
        // given
        AuthUser otherUser = new AuthUser(2L, "other@gmail.com", Role.ROLE_USER);
        given(scheduleRepository.findByIdWithCompany(schedule.getId())).willReturn(Optional.of(schedule));

        // when & then
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> scheduleService.findSchedule(otherUser, schedule.getId()));
        assertThat(exception.getMessage()).isEqualTo("해당 리소스에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("해당 사용자가 생성한 모든 일정을 조회한다")
    void findAllSchedules_success() {
        // given
        Schedule schedule2 = Schedule.builder()
                .id(101L)
                .user(user)
                .company(company)
                .dueDate(LocalDateTime.of(2024, 6, 2, 14, 0))
                .step(Step.SECOND_INTERVIEW)
                .position("프론트엔드")
                .memo("2차 면접")
                .build();

        List<Schedule> scheduleList = List.of(schedule, schedule2);
        given(scheduleRepository.findByUserIdWithCompany(authUser.getId())).willReturn(scheduleList);

        // when
        ScheduleListResponse response = scheduleService.findAllSchedules(authUser);

        // then
        assertThat(response.getScheduleList()).hasSize(2);
        assertThat(response.getScheduleList()).extracting("position").contains("백엔드", "프론트엔드");
    }

    @Test
    @DisplayName("해당 사용자가 소유한 일정이 없으면 빈 리스트를 반환한다")
    void findAllSchedules_empty() {
        // given
        given(scheduleRepository.findByUserIdWithCompany(authUser.getId())).willReturn(List.of());

        // when
        ScheduleListResponse response = scheduleService.findAllSchedules(authUser);

        // then
        assertThat(response.getScheduleList()).isEmpty();
    }

    @Test
    @DisplayName("일정 생성에 성공한다")
    void createSchedule_success() {
        // given
        ScheduleCreateRequest request = new ScheduleCreateRequest(
                company.getId(),
                company.getCompanyName(),
                schedule.getStep(),
                schedule.getDueDate(),
                schedule.getPosition(),
                schedule.getMemo()
        );
        given(companyRepository.findById(company.getId())).willReturn(Optional.of(company));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(scheduleRepository.save(any(Schedule.class))).willReturn(schedule);

        // when
        ScheduleResponse response = scheduleService.createSchedule(authUser, request);

        // then
        assertThat(response.getCompanyId()).isEqualTo(company.getId());
        assertThat(response.getCompanyName()).isEqualTo(company.getCompanyName());
        assertThat(response.getStep()).isEqualTo(schedule.getStep().getDisplayName());
        assertThat(response.getPosition()).isEqualTo(schedule.getPosition());
        assertThat(response.getMemo()).isEqualTo(schedule.getMemo());
        assertThat(response.getDueDate()).isEqualTo(schedule.getDueDate());
    }

    @Test
    @DisplayName("일정 생성 시 소유자가 아니면 예외가 발생한다")
    void createSchedule_fail_notOwner() {
        // given
        AuthUser otherUser = new AuthUser(2L, "other@gmail.com", Role.ROLE_USER);
        ScheduleCreateRequest request = new ScheduleCreateRequest(
                company.getId(),
                company.getCompanyName(),
                schedule.getStep(),
                schedule.getDueDate(),
                schedule.getPosition(),
                schedule.getMemo()
        );
        given(companyRepository.findById(company.getId())).willReturn(Optional.of(company));

        // when & then
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> scheduleService.createSchedule(otherUser, request));
        assertThat(exception.getMessage()).isEqualTo("해당 리소스에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("일정 수정에 성공한다")
    void updateSchedule_success() {
        // given
        ScheduleUpdateRequest updateRequest = new ScheduleUpdateRequest(
                Step.DOCUMENT,
                LocalDateTime.of(2025, 6, 12, 10, 0),
                "수정된 메모",
                "프론트엔드"
        );
        given(scheduleRepository.findById(schedule.getId())).willReturn(Optional.of(schedule));

        // when
        scheduleService.updateSchedule(authUser, schedule.getId(), updateRequest);

        // then
        assertThat(schedule.getDueDate()).isEqualTo(updateRequest.getDueDate());
        assertThat(schedule.getMemo()).isEqualTo(updateRequest.getMemo());
        assertThat(schedule.getPosition()).isEqualTo(updateRequest.getPosition());
        assertThat(schedule.getStep()).isEqualTo(Step.DOCUMENT);
    }

    @Test
    @DisplayName("일정 수정 시 소유자가 아니면 예외가 발생한다")
    void updateSchedule_fail_notOwner() {
        // given
        AuthUser otherUser = new AuthUser(2L, "other@gmail.com", Role.ROLE_USER);
        ScheduleUpdateRequest updateRequest = new ScheduleUpdateRequest(
                Step.DOCUMENT, LocalDateTime.now(), "백엔드", "메모 수정"
        );
        given(scheduleRepository.findById(schedule.getId())).willReturn(Optional.of(schedule));

        // when & then
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> scheduleService.updateSchedule(otherUser, schedule.getId(), updateRequest));
        assertThat(exception.getMessage()).isEqualTo("해당 리소스에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("일정 삭제에 성공한다")
    void deleteSchedule_success() {
        // given
        given(scheduleRepository.findById(schedule.getId())).willReturn(Optional.of(schedule));

        // when
        scheduleService.deleteSchedule(authUser, schedule.getId());

        // then
        verify(scheduleRepository).delete(schedule);
    }

    @Test
    @DisplayName("일정 삭제 시 소유자가 아니면 예외가 발생한다")
    void deleteSchedule_fail_notOwner() {
        // given
        AuthUser otherUser = new AuthUser(2L, "other@gmail.com", Role.ROLE_USER);
        given(scheduleRepository.findById(schedule.getId())).willReturn(Optional.of(schedule));

        // when & then
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> scheduleService.deleteSchedule(otherUser, schedule.getId()));
        assertThat(exception.getMessage()).isEqualTo("해당 리소스에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("날짜 범위별 일정 조회에 성공한다")
    void findSchedulesInRange_success() {
        // given
        LocalDate startDate = LocalDate.of(2025, 5, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 31);

        Schedule schedule2 = Schedule.builder()
                .id(101L)
                .user(user)
                .company(company)
                .companyName(company.getCompanyName())
                .dueDate(LocalDateTime.of(2025, 6, 2, 14, 0))
                .step(Step.SECOND_INTERVIEW)
                .position("프론트엔드")
                .memo("2차 면접")
                .build();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        given(scheduleRepository.findSchedulesInRange(authUser.getId(), startDateTime, endDateTime))
                .willReturn(List.of(schedule, schedule2));

        // when
        ScheduleListResponse response = scheduleService.findSchedulesInRange(authUser, startDate, endDate);

        // then
        assertThat(response.getScheduleList()).hasSize(2);

        ScheduleResponse resp1 = response.getScheduleList().get(0);
        assertThat(resp1.getCompanyId()).isEqualTo(company.getId());
        assertThat(resp1.getCompanyName()).isEqualTo(company.getCompanyName());
        assertThat(resp1.getStep()).isEqualTo(schedule.getStep().getDisplayName());
        assertThat(resp1.getPosition()).isEqualTo(schedule.getPosition());
        assertThat(resp1.getMemo()).isEqualTo(schedule.getMemo());
        assertThat(resp1.getDueDate()).isEqualTo(schedule.getDueDate());

        ScheduleResponse resp2 = response.getScheduleList().get(1);
        assertThat(resp2.getCompanyId()).isEqualTo(company.getId());
        assertThat(resp2.getCompanyName()).isEqualTo(company.getCompanyName());
        assertThat(resp2.getStep()).isEqualTo(schedule2.getStep().getDisplayName());
        assertThat(resp2.getPosition()).isEqualTo(schedule2.getPosition());
        assertThat(resp2.getMemo()).isEqualTo(schedule2.getMemo());
        assertThat(resp2.getDueDate()).isEqualTo(schedule2.getDueDate());
    }

}