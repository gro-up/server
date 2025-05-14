package com.hamster.gro_up.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamster.gro_up.config.*;
import com.hamster.gro_up.dto.request.RetrospectCreateRequest;
import com.hamster.gro_up.dto.request.RetrospectUpdateRequest;
import com.hamster.gro_up.dto.response.RetrospectListResponse;
import com.hamster.gro_up.dto.response.RetrospectResponse;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.exception.retrospect.RetrospectNotFoundException;
import com.hamster.gro_up.service.CustomOAuth2UserService;
import com.hamster.gro_up.service.RetrospectService;
import com.hamster.gro_up.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RetrospectController.class)
@Import({SecurityConfig.class, JwtUtil.class})
class RetrospectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RetrospectService retrospectService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Test
    @DisplayName("회고 단건 조회에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void findRetrospect_success() throws Exception {
        // given
        RetrospectResponse response = new RetrospectResponse(
                100L, "회고 메모", 10L, "ham-corp", "백엔드", LocalDateTime.now()
        );
        given(retrospectService.findRetrospect(any(), eq(200L))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/retrospects/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.scheduleId").value(100L))
                .andExpect(jsonPath("$.data.companyId").value(10L));
    }

    @Test
    @DisplayName("존재하지 않는 회고 조회 시 404를 반환한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void findRetrospect_notFound() throws Exception {
        // given
        given(retrospectService.findRetrospect(any(), eq(999L)))
                .willThrow(new RetrospectNotFoundException());

        // when & then
        mockMvc.perform(get("/api/retrospects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("해당 사용자의 모든 회고 조회에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void findAllRetrospects_success() throws Exception {
        // given
        RetrospectResponse retrospect1 = new RetrospectResponse(
                100L, "회고1", 10L, "ham-corp", "백엔드", LocalDateTime.now()
        );
        RetrospectResponse retrospect2 = new RetrospectResponse(
                101L, "회고2", 11L, "egg-corp", "프론트엔드", LocalDateTime.now()
        );
        RetrospectListResponse response = RetrospectListResponse.of(List.of(retrospect1, retrospect2));
        given(retrospectService.findAllRetrospects(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/retrospects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.retrospectList").isArray())
                .andExpect(jsonPath("$.data.retrospectList[0].scheduleId").value(100L))
                .andExpect(jsonPath("$.data.retrospectList[1].scheduleId").value(101L));
    }

    @Test
    @DisplayName("회고 생성에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void createRetrospect_success() throws Exception {
        // given
        RetrospectCreateRequest request = new RetrospectCreateRequest(
                100L, "회고 메모"
        );
        RetrospectResponse response = new RetrospectResponse(
                100L, "회고 메모", 10L, "ham-corp", "백엔드", LocalDateTime.now()
        );
        given(retrospectService.createRetrospect(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/retrospects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.scheduleId").value(100L));
    }

    @Test
    @DisplayName("회고 수정에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void updateRetrospect_success() throws Exception {
        // given
        RetrospectUpdateRequest updateRequest = new RetrospectUpdateRequest("수정된 회고 메모");
        willDoNothing().given(retrospectService).updateRetrospect(any(), eq(200L), any());

        // when & then
        mockMvc.perform(put("/api/retrospects/200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회고 삭제에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void deleteRetrospect_success() throws Exception {
        // given
        willDoNothing().given(retrospectService).deleteRetrospect(any(), eq(200L));

        // when & then
        mockMvc.perform(delete("/api/retrospects")
                        .param("retrospectId", "200"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회고 목록이 비어 있을 때 빈 리스트가 반환된다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void findAllRetrospects_empty() throws Exception {
        // given
        RetrospectListResponse emptyResponse = RetrospectListResponse.of(List.of());
        given(retrospectService.findAllRetrospects(any())).willReturn(emptyResponse);

        // when & then
        mockMvc.perform(get("/api/retrospects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.retrospectList").isArray())
                .andExpect(jsonPath("$.data.retrospectList").isEmpty());
    }

    @Test
    @DisplayName("일정 id 가 누락된 회고 생성 요청 시 400을 반환한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", role = Role.ROLE_USER)
    void createRetrospect_fail_validation() throws Exception {
        // given: scheduleId 가 누락된 경우
        RetrospectCreateRequest invalidRequest = new RetrospectCreateRequest(
                null,
                "memo"
        );

        // when & then
        mockMvc.perform(post("/api/retrospects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}