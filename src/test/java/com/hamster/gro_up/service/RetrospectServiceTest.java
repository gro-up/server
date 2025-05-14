package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.RetrospectCreateRequest;
import com.hamster.gro_up.dto.request.RetrospectUpdateRequest;
import com.hamster.gro_up.dto.response.RetrospectListResponse;
import com.hamster.gro_up.dto.response.RetrospectResponse;
import com.hamster.gro_up.entity.Retrospect;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.entity.Schedule;
import com.hamster.gro_up.entity.User;
import com.hamster.gro_up.exception.ForbiddenException;
import com.hamster.gro_up.exception.retrospect.RetrospectNotFoundException;
import com.hamster.gro_up.exception.schedule.ScheduleNotFoundException;
import com.hamster.gro_up.repository.RetrospectRepository;
import com.hamster.gro_up.repository.ScheduleRepository;
import com.hamster.gro_up.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RetrospectServiceTest {


    @Mock
    private RetrospectRepository retrospectRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RetrospectService retrospectService;

    private User user;
    private Schedule schedule;
    private Retrospect retrospect;
    private AuthUser authUser;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .build();

        schedule = Schedule.builder()
                .id(100L)
                .user(user)
                .company(null)
                .companyName("테스트회사")
                .position("백엔드")
                .memo("스케줄 메모")
                .build();

        retrospect = Retrospect.builder()
                .id(200L)
                .memo("회고 메모")
                .schedule(schedule)
                .user(user)
                .build();

        authUser = new AuthUser(1L, "ham@gmail.com", Role.ROLE_USER);
    }

    @Test
    @DisplayName("회고 단건 조회에 성공한다")
    void findRetrospect_success() {
        // given
        given(retrospectRepository.findByIdWithSchedule(retrospect.getId()))
                .willReturn(Optional.of(retrospect));

        // when
        RetrospectResponse response = retrospectService.findRetrospect(authUser, retrospect.getId());

        // then
        assertThat(response.getScheduleId()).isEqualTo(schedule.getId());
        assertThat(response.getMemo()).isEqualTo(retrospect.getMemo());
        assertThat(response.getCompanyId()).isNull();
        assertThat(response.getCompanyName()).isEqualTo(schedule.getCompanyName());
        assertThat(response.getPosition()).isEqualTo(schedule.getPosition());
        assertThat(response.getCreatedAt()).isEqualTo(retrospect.getCreatedAt());
    }

    @Test
    @DisplayName("존재하지 않는 회고 조회 시 예외가 발생한다")
    void findRetrospect_fail_notExist() {
        // given
        given(retrospectRepository.findByIdWithSchedule(999L)).willReturn(Optional.empty());

        // when & then
        RetrospectNotFoundException exception = assertThrows(RetrospectNotFoundException.class,
                () -> retrospectService.findRetrospect(authUser, 999L));
        assertThat(exception.getMessage()).isEqualTo("해당 회고를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("회고 조회 시 소유자가 아니면 예외가 발생한다")
    void findRetrospect_fail_notOwner() {
        // given
        AuthUser otherUser = new AuthUser(2L, "other@gmail.com", Role.ROLE_USER);
        given(retrospectRepository.findByIdWithSchedule(retrospect.getId())).willReturn(Optional.of(retrospect));

        // when & then
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> retrospectService.findRetrospect(otherUser, retrospect.getId()));
        assertThat(exception.getMessage()).isEqualTo("해당 리소스에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("해당 사용자가 생성한 모든 회고를 조회한다")
    void findAllRetrospects_success() {
        // given
        Retrospect retrospect2 = Retrospect.builder()
                .id(201L)
                .memo("두번째 회고")
                .schedule(schedule)
                .user(user)
                .build();

        List<Retrospect> retrospectList = List.of(retrospect, retrospect2);
        given(retrospectRepository.findAllByUserIdWithSchedule(authUser.getId())).willReturn(retrospectList);

        // when
        RetrospectListResponse response = retrospectService.findAllRetrospects(authUser);

        // then
        assertThat(response.getRetrospectList()).hasSize(2);
        assertThat(response.getRetrospectList()).extracting("memo").contains("회고 메모", "두번째 회고");
    }

    @Test
    @DisplayName("해당 사용자가 소유한 회고가 없으면 빈 리스트를 반환한다")
    void findAllRetrospects_empty() {
        // given
        given(retrospectRepository.findAllByUserIdWithSchedule(authUser.getId())).willReturn(List.of());

        // when
        RetrospectListResponse response = retrospectService.findAllRetrospects(authUser);

        // then
        assertThat(response.getRetrospectList()).isEmpty();
    }

    @Test
    @DisplayName("회고 생성에 성공한다")
    void createRetrospect_success() {
        // given
        RetrospectCreateRequest request = new RetrospectCreateRequest(
                schedule.getId(),
                "새 회고 메모"
        );
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(scheduleRepository.findByIdWithCompany(schedule.getId())).willReturn(Optional.of(schedule));
        given(retrospectRepository.save(any(Retrospect.class))).willReturn(retrospect);

        // when
        RetrospectResponse response = retrospectService.createRetrospect(authUser, request);

        // then
        assertThat(response.getScheduleId()).isEqualTo(schedule.getId());
        assertThat(response.getMemo()).isEqualTo(retrospect.getMemo());
    }

    @Test
    @DisplayName("회고 생성 시 존재하지 않는 스케줄이면 예외가 발생한다")
    void createRetrospect_fail_noSchedule() {
        // given
        RetrospectCreateRequest request = new RetrospectCreateRequest(999L, "메모");
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(scheduleRepository.findByIdWithCompany(999L)).willReturn(Optional.empty());

        // when & then
        ScheduleNotFoundException exception = assertThrows(ScheduleNotFoundException.class,
                () -> retrospectService.createRetrospect(authUser, request));
        assertThat(exception.getMessage()).isEqualTo("해당 일정을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("회고 수정에 성공한다")
    void updateRetrospect_success() {
        // given
        RetrospectUpdateRequest updateRequest = new RetrospectUpdateRequest("수정된 회고 메모");
        given(retrospectRepository.findById(retrospect.getId())).willReturn(Optional.of(retrospect));

        // when
        retrospectService.updateRetrospect(authUser, retrospect.getId(), updateRequest);

        // then
        assertThat(retrospect.getMemo()).isEqualTo(updateRequest.getMemo());
    }

    @Test
    @DisplayName("회고 수정 시 소유자가 아니면 예외가 발생한다")
    void updateRetrospect_fail_notOwner() {
        // given
        AuthUser otherUser = new AuthUser(2L, "other@gmail.com", Role.ROLE_USER);
        RetrospectUpdateRequest updateRequest = new RetrospectUpdateRequest("수정된 메모");
        given(retrospectRepository.findById(retrospect.getId())).willReturn(Optional.of(retrospect));

        // when & then
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> retrospectService.updateRetrospect(otherUser, retrospect.getId(), updateRequest));
        assertThat(exception.getMessage()).isEqualTo("해당 리소스에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("회고 삭제에 성공한다")
    void deleteRetrospect_success() {
        // given
        given(retrospectRepository.findById(retrospect.getId())).willReturn(Optional.of(retrospect));

        // when
        retrospectService.deleteRetrospect(authUser, retrospect.getId());

        // then
        verify(retrospectRepository).delete(retrospect);
    }

    @Test
    @DisplayName("회고 삭제 시 소유자가 아니면 예외가 발생한다")
    void deleteRetrospect_fail_notOwner() {
        // given
        AuthUser otherUser = new AuthUser(2L, "other@gmail.com", Role.ROLE_USER);
        given(retrospectRepository.findById(retrospect.getId())).willReturn(Optional.of(retrospect));

        // when & then
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> retrospectService.deleteRetrospect(otherUser, retrospect.getId()));
        assertThat(exception.getMessage()).isEqualTo("해당 리소스에 접근할 권한이 없습니다.");
    }
}