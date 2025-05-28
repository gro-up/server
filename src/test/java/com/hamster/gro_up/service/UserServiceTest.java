package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.ImageUpdateRequest;
import com.hamster.gro_up.dto.response.UserResponse;
import com.hamster.gro_up.entity.User;
import com.hamster.gro_up.exception.user.UserNotFoundException;
import com.hamster.gro_up.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private UserService userService;

    private User user;
    private AuthUser authUser;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@test.com")
                .imageUrl("https://bucket/profile1.png")
                .build();

        authUser = AuthUser.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }

    @Test
    @DisplayName("자기 자신 조회에 성공한다")
    void findUser_success() {
        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.findUser(authUser);

        // then
        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외가 발생한다")
    void findUser_fail_notExist() {
        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findUser(authUser)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("프로필 이미지를 새로 등록하면 기존 이미지는 삭제되고 새 이미지로 변경된다")
    void updateProfileImage_success_change() {
        // given
        String oldImageUrl = user.getImageUrl();
        String newImageUrl = "https://bucket/profile2.png";
        ImageUpdateRequest request = new ImageUpdateRequest(newImageUrl);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        // when
        userService.updateProfileImage(authUser, request);

        // then
        verify(imageService).deleteImage(oldImageUrl);
        assertThat(user.getImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    @DisplayName("프로필 이미지를 삭제하면 기존 이미지도 삭제되고 DB에는 null이 저장된다")
    void updateProfileImage_success_remove() {
        // given
        String oldImageUrl = user.getImageUrl();
        ImageUpdateRequest request = new ImageUpdateRequest(null);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        // when
        userService.updateProfileImage(authUser, request);

        // then
        verify(imageService).deleteImage(oldImageUrl);
        assertThat(user.getImageUrl()).isNull();
    }

    @Test
    @DisplayName("프로필 이미지가 변경되지 않으면 삭제가 일어나지 않는다")
    void updateProfileImage_noChange() {
        // given
        String oldImageUrl = user.getImageUrl();
        ImageUpdateRequest request = new ImageUpdateRequest(oldImageUrl);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        // when
        userService.updateProfileImage(authUser, request);

        // then
        verify(imageService, never()).deleteImage(any());
        assertThat(user.getImageUrl()).isEqualTo(oldImageUrl);
    }
}