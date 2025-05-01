package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.CompanyCreateRequest;
import com.hamster.gro_up.dto.request.CompanyUpdateRequest;
import com.hamster.gro_up.dto.response.CompanyListResponse;
import com.hamster.gro_up.dto.response.CompanyResponse;
import com.hamster.gro_up.entity.Company;
import com.hamster.gro_up.entity.Role;
import com.hamster.gro_up.entity.User;
import com.hamster.gro_up.exception.ForbiddenException;
import com.hamster.gro_up.exception.company.CompanyNotFoundException;
import com.hamster.gro_up.repository.CompanyRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompanyService companyService;

    private User user;
    private Company company;
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

        authUser = new AuthUser(1L, "ham@gmail.com", "ham", Role.ROLE_USER);
    }

    @Test
    @DisplayName("기업 등록에 성공한다")
    void createCompany_success() {
        // given
        CompanyCreateRequest requestDto = new CompanyCreateRequest("ham-corp", "back-end", "www.ham.com", "seoul");
        given(companyRepository.save(any(Company.class))).willReturn(company);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        CompanyResponse companyResponse = companyService.createCompany(authUser, requestDto);

        // then
        assertThat(companyResponse.getCompanyName()).isEqualTo("ham-corp");
        assertThat(companyResponse.getPosition()).isEqualTo("back-end");
        assertThat(companyResponse.getUrl()).isEqualTo("www.ham.com");
        assertThat(companyResponse.getLocation()).isEqualTo("seoul");
    }

    @Test
    @DisplayName("기업 조회에 성공한다")
    void findCompany_success() {
        // given
        Long companyId = 1L;
        given(companyRepository.findById(companyId)).willReturn(Optional.of(company));

        // when
        CompanyResponse companyResponse = companyService.findCompany(authUser, companyId);

        // then
        assertThat(companyResponse.getCompanyName()).isEqualTo("ham-corp");
        assertThat(companyResponse.getPosition()).isEqualTo("back-end");
        assertThat(companyResponse.getUrl()).isEqualTo("www.ham.com");
        assertThat(companyResponse.getLocation()).isEqualTo("seoul");
    }

    @Test
    @DisplayName("존재하지 않는 회사 조회 시 예외가 발생한다")
    void findCompany_fail_notExist() {
        // given
        long companyId = 999L;
        given(companyRepository.findById(companyId)).willReturn(Optional.empty());

        // when & then
        CompanyNotFoundException exception = assertThrows(CompanyNotFoundException.class, () -> companyService.findCompany(authUser, companyId));
        assertThat(exception.getMessage()).isEqualTo("해당 기업을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("기업 수정에 성공한다")
    void updateCompany_success() {
        // given
        long companyId = 10L;
        CompanyUpdateRequest updateRequest = new CompanyUpdateRequest("new-corp", "front-end", "www.new.com", "busan");
        given(companyRepository.findById(companyId)).willReturn(Optional.of(company));

        // when
        companyService.updateCompany(authUser, companyId, updateRequest);

        // then
        assertThat(company.getCompanyName()).isEqualTo("new-corp");
        assertThat(company.getPosition()).isEqualTo("front-end");
        assertThat(company.getLocation()).isEqualTo("busan");
        assertThat(company.getUrl()).isEqualTo("www.new.com");
    }

    @Test
    @DisplayName("기업 수정 시 소유자가 아니면 예외가 발생한다")
    void updateCompany_fail_notOwner() {
        // given
        AuthUser otherAuthUser = new AuthUser(2L, "other-user", "other-auth-user", Role.ROLE_USER);
        CompanyUpdateRequest updateRequest = new CompanyUpdateRequest("new-corp", "front-end", "busan", "www.new.com");
        given(companyRepository.findById(10L)).willReturn(Optional.of(company));

        // when & then
        RuntimeException exception = assertThrows(ForbiddenException.class, () -> {
            companyService.updateCompany(otherAuthUser, 10L, updateRequest);
        });
        assertThat(exception.getMessage()).isEqualTo("해당 리소스에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("기업 삭제에 성공한다")
    void deleteCompany_success() {
        // given
        given(companyRepository.findById(10L)).willReturn(Optional.of(company));

        // when
        companyService.deleteCompany(authUser, 10L);

        // then
        verify(companyRepository).delete(company);
    }

    @Test
    @DisplayName("기업 삭제 시 소유자가 아니면 예외가 발생한다")
    void deleteCompany_fail_notOwner() {
        // given
        AuthUser otherAuthUser = new AuthUser(2L, "other-user", "other-auth-user", Role.ROLE_USER);
        given(companyRepository.findById(10L)).willReturn(Optional.of(company));

        // when & then
        RuntimeException exception = assertThrows(ForbiddenException.class, () -> {
            companyService.deleteCompany(otherAuthUser, 10L);
        });
        assertThat(exception.getMessage()).isEqualTo("해당 리소스에 접근할 권한이 없습니다.");
    }

    @Test
    @DisplayName("해당 사용자가 생성한 모든 기업을 조회한다")
    void findAllCompany_success() {
        // given
        Company company2 = Company.builder()
                .id(2L)
                .companyName("ham-corp")
                .position("back-end")
                .url("www.ham.com")
                .location("seoul")
                .build();

        Company company3 = Company.builder()
                .id(3L)
                .companyName("egg-corp")
                .position("front-end")
                .url("www.egg.com")
                .location("busan")
                .build();

        List<Company> companyList = List.of(company2, company3);

        given(companyRepository.findByUserId(authUser.getId())).willReturn(companyList);

        // when
        CompanyListResponse response = companyService.findAllCompany(authUser);

        // then
        assertThat(response.getCompanyList()).hasSize(2);
        assertThat(response.getCompanyList()).extracting("companyName").containsExactlyInAnyOrder("ham-corp", "egg-corp");
    }

    @Test
    @DisplayName("해당 유저가 소유한 기업이 없으면 빈 리스트를 반환한다")
    void findAllCompany_empty() {
        // given
        given(companyRepository.findByUserId(authUser.getId())).willReturn(List.of());

        // when
        CompanyListResponse response = companyService.findAllCompany(authUser);

        // then
        assertThat(response.getCompanyList()).isEmpty();
    }
}
