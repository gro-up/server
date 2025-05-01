package com.hamster.gro_up.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamster.gro_up.config.CustomOAuth2SuccessHandler;
import com.hamster.gro_up.config.SecurityConfig;
import com.hamster.gro_up.config.WithMockAuthUser;
import com.hamster.gro_up.dto.request.CompanyCreateRequest;
import com.hamster.gro_up.dto.request.CompanyUpdateRequest;
import com.hamster.gro_up.dto.response.CompanyResponse;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.exception.NotFoundException;
import com.hamster.gro_up.exception.company.CompanyNotFoundException;
import com.hamster.gro_up.service.CompanyService;
import com.hamster.gro_up.service.CustomOAuth2UserService;
import com.hamster.gro_up.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
@Import({SecurityConfig.class, JwtUtil.class})
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @Test
    @DisplayName("기업 조회에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", name = "ham", role = Role.ROLE_USER)
    void findCompany_success() throws Exception {
        // given
        CompanyResponse response = new CompanyResponse(10L, "ham-corp", "back-end", "www.ham.com", "seoul");
        given(companyService.findCompany(any(), eq(10L))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/companies/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.companyId").value(10L));
    }

    @Test
    @DisplayName("기업 등록에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", name = "ham", role = Role.ROLE_USER)
    void createCompany_success() throws Exception {
        // given
        CompanyCreateRequest request = new CompanyCreateRequest("ham-corp", "back-end", "www.ham.com", "seoul");
        CompanyResponse response = new CompanyResponse(10L, "ham-corp", "back-end", "www.ham.com", "seoul");
        given(companyService.createCompany(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("code").value(201))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.companyId").value(10L));
    }

    @Test
    @DisplayName("기업 수정에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", name = "ham", role = Role.ROLE_USER)
    void updateCompany_success() throws Exception {
        // given
        CompanyUpdateRequest updateRequest = new CompanyUpdateRequest("new-corp", "front-end", "www.new.com", "busan");
        willDoNothing().given(companyService).updateCompany(any(), eq(10L), any());

        // when & then
        mockMvc.perform(put("/api/companies/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("기업 삭제에 성공한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", name = "ham", role = Role.ROLE_USER)
    void deleteCompany_success() throws Exception {
        // given
        willDoNothing().given(companyService).deleteCompany(any(), eq(10L));

        // when & then
        mockMvc.perform(delete("/api/companies/10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 기업 조회 시 예외가 발생한다")
    @WithMockAuthUser(userId = 1L, email = "ham@example.com", name = "ham", role = Role.ROLE_USER)
    void findCompany_notFound() throws Exception {
        // given
        given(companyService.findCompany(any(), eq(999L)))
                .willThrow(new CompanyNotFoundException());

        // when & then
        mockMvc.perform(get("/api/companies/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("해당 기업을 찾을 수 없습니다."));
    }
}