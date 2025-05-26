package com.hamster.gro_up.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamster.gro_up.config.*;
import com.hamster.gro_up.dto.request.ScheduleCreateRequest;
import com.hamster.gro_up.dto.request.ScheduleUpdateRequest;
import com.hamster.gro_up.dto.response.ScheduleListResponse;
import com.hamster.gro_up.dto.response.ScheduleResponse;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.entity.Step;
import com.hamster.gro_up.exception.schedule.ScheduleNotFoundException;
import com.hamster.gro_up.service.CustomOAuth2UserService;
import com.hamster.gro_up.service.ScheduleService;
import com.hamster.gro_up.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleController.class)
@Import({SecurityConfig.class, JwtUtil.class})
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    @DisplayName("일정 단건 조회에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void findSchedule_success() throws Exception {
        // given
        ScheduleResponse response = new ScheduleResponse(
                1L,
                10L,
                "ham-corp",
                "서울",
                "상세 주소",
                "DOCUMENT",
                "백엔드",
                "메모",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        given(scheduleService.findSchedule(any(), eq(100L))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/schedules/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.companyId").value(10L));
    }

    @Test
    @DisplayName("존재하지 않는 일정 조회 시 404를 반환한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void findSchedule_notFound() throws Exception {
        // given
        given(scheduleService.findSchedule(any(), eq(999L)))
                .willThrow(new ScheduleNotFoundException());

        // when & then
        mockMvc.perform(get("/api/schedules/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("해당 사용자의 모든 일정 조회에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void findAllSchedules_success() throws Exception {
        // given
        ScheduleResponse schedule1 = new ScheduleResponse(
                1L,
                10L,
                "ham-corp",
                "서울",
                "상세주소",
                "DOCUMENT",
                "백엔드",
                "메모1",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        ScheduleResponse schedule2 = new ScheduleResponse(
                1L,
                11L,
                "egg-corp",
                "서울",
                "상세주소",
                "INTERVIEW",
                "프론트엔드",
                "메모2",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        ScheduleListResponse response = ScheduleListResponse.of(List.of(schedule1, schedule2));
        given(scheduleService.findAllSchedules(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.scheduleList").isArray())
                .andExpect(jsonPath("$.data.scheduleList[0].companyId").value(10L))
                .andExpect(jsonPath("$.data.scheduleList[1].companyId").value(11L));
    }

    @Test
    @DisplayName("일정 생성에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void createSchedule_success() throws Exception {
        // given
        ScheduleCreateRequest request = new ScheduleCreateRequest(
                10L, "ham-corp", "서울", "상세주소", Step.DOCUMENT, LocalDateTime.now(), " 백엔드", "메모"
        );
        ScheduleResponse response = new ScheduleResponse(
                1L,
                10L,
                "ham-corp",
                "서울",
                "상세주소",
                "DOCUMENT",
                "백엔드",
                "메모",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        given(scheduleService.createSchedule(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.companyId").value(10L));
    }

    @Test
    @DisplayName("일정 수정에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void updateSchedule_success() throws Exception {
        // given
        ScheduleUpdateRequest updateRequest = new ScheduleUpdateRequest(
                null, "test-corp", "test-addrss", "test-addressDetail", Step.DOCUMENT, LocalDateTime.now(), "프론트엔드", "수정 메모"
        );
        willDoNothing().given(scheduleService).updateSchedule(any(), eq(100L), any());

        // when & then
        mockMvc.perform(put("/api/schedules/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("일정 삭제에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void deleteSchedule_success() throws Exception {
        // given
        willDoNothing().given(scheduleService).deleteSchedule(any(), eq(100L));

        // when & then
        mockMvc.perform(delete("/api/schedules/100"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("일정 목록이 비어 있을 때 빈 리스트가 반환된다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void findAllSchedules_empty() throws Exception {
        // given
        ScheduleListResponse emptyResponse = ScheduleListResponse.of(List.of());
        given(scheduleService.findAllSchedules(any())).willReturn(emptyResponse);

        // when & then
        mockMvc.perform(get("/api/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.scheduleList").isArray())
                .andExpect(jsonPath("$.data.scheduleList").isEmpty());
    }

    @Test
    @DisplayName("필수값이 누락된 일정 생성 요청 시 400을 반환한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void createSchedule_fail_validation() throws Exception {
        // given: companyName 이 null 또는 빈 문자열(필수값 누락)
        ScheduleCreateRequest invalidRequest = new ScheduleCreateRequest(
                null,           // companyId
                "",             // companyName (NotBlank 위반)
                "서울",
                "상세 주소",
                null,           // step
                null,           // dueDate
                null,           // position
                null            // memo
        );

        // when & then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("날짜 범위별 일정 조회에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void getSchedulesByDateRange_success() throws Exception {
        // given
        ScheduleResponse schedule1 = new ScheduleResponse(
                1L, 1L, "ham-corp", "서울", "상세주소", "DOCUMENT", "백엔드", "메모",
                LocalDateTime.of(2025, 5, 10, 10, 0),
                LocalDateTime.of(2025, 5, 10, 11, 0),
                LocalDateTime.of(2025, 5, 10, 10, 0)
        );
        ScheduleResponse schedule2 = new ScheduleResponse(
                2L, 2L, "ham-corp", "서울", "상세주소", "INTERVIEW", "프론트", "면접",
                LocalDateTime.of(2025, 5, 20, 14, 0),
                LocalDateTime.of(2025, 5, 20, 15, 0),
                LocalDateTime.of(2025, 5, 20, 14, 0)
        );
        ScheduleListResponse response = ScheduleListResponse.of(List.of(schedule1, schedule2));
        given(scheduleService.findSchedulesInRange(any(), eq(LocalDate.of(2025, 5, 1)), eq(LocalDate.of(2025, 5, 31))))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/schedules/range")
                        .param("start", "20250501")
                        .param("end", "20250531")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.scheduleList").isArray())
                .andExpect(jsonPath("$.data.scheduleList[0].companyId").value(1))
                .andExpect(jsonPath("$.data.scheduleList[0].companyName").value("ham-corp"))
                .andExpect(jsonPath("$.data.scheduleList[1].companyId").value(2))
                .andExpect(jsonPath("$.data.scheduleList[1].companyName").value("ham-corp"));
    }

    @Test
    @DisplayName("날짜 범위별 일정이 없으면 빈 리스트가 반환된다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void getSchedulesByDateRange_empty() throws Exception {
        // given
        ScheduleListResponse emptyResponse = ScheduleListResponse.of(List.of());
        given(scheduleService.findSchedulesInRange(any(), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(emptyResponse);

        // when & then
        mockMvc.perform(get("/api/schedules/range")
                        .param("start", "20250501")
                        .param("end", "20250531")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.scheduleList").isArray())
                .andExpect(jsonPath("$.data.scheduleList").isEmpty());
    }

    @Test
    @DisplayName("필수값 누락시 400을 반환한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void getSchedulesByDateRange_fail_validation() throws Exception {
        // when & then
        mockMvc.perform(get("/api/schedules/range")
                        .param("start", "") // 빈 값
                        .param("end", "20250531")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("정확한 기업명으로 일정 검색에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void findSchedulesByCompanyName_success() throws Exception {
        // given
        String companyName = "네이버";
        ScheduleResponse schedule1 = new ScheduleResponse(
                1L, 10L, companyName, "서울", "상세주소", "DOCUMENT", "백엔드", "메모1",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );
        ScheduleResponse schedule2 = new ScheduleResponse(
                2L, 10L, companyName, "서울", "상세 주소", "INTERVIEW", "프론트엔드", "메모2",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );
        ScheduleListResponse response = ScheduleListResponse.of(List.of(schedule1, schedule2));
        given(scheduleService.findSchedulesByCompanyName(any(), eq(companyName))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/schedules/search")
                        .param("companyName", companyName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.scheduleList").isArray())
                .andExpect(jsonPath("$.data.scheduleList[0].companyName").value(companyName))
                .andExpect(jsonPath("$.data.scheduleList[1].companyName").value(companyName));
    }
}