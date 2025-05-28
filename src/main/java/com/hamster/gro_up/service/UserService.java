package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.AuthUser;
import com.hamster.gro_up.dto.request.ImageUpdateRequest;
import com.hamster.gro_up.dto.response.UserResponse;
import com.hamster.gro_up.entity.User;
import com.hamster.gro_up.exception.image.ImageException;
import com.hamster.gro_up.exception.user.UserNotFoundException;
import com.hamster.gro_up.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ImageService imageService;

    public UserResponse findUser(AuthUser authUser) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(UserNotFoundException::new);
        return UserResponse.from(user);
    }

    @Transactional
    public void updateProfileImage(AuthUser authUser, ImageUpdateRequest imageUpdateRequest) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(UserNotFoundException::new);

        String oldImageUrl = user.getImageUrl();
        String newImageUrl = imageUpdateRequest.getImageUrl();

        // 프로필 이미지를 내리는 경우 (newImageUrl이 null 또는 빈 값)
        if (newImageUrl == null || newImageUrl.isBlank()) {
            // 기존 이미지가 있다면 삭제
            if (oldImageUrl != null && !oldImageUrl.isBlank()) {
                    imageService.deleteImage(oldImageUrl);
            }
            user.updateImageUrl(null); // DB 에서도 이미지 정보 제거
            return;
        }

        // 기존 이미지와 다를 때만 삭제 및 업데이트
        if (oldImageUrl != null && !oldImageUrl.isBlank() && !oldImageUrl.equals(newImageUrl)) {
                imageService.deleteImage(oldImageUrl);
        }

        user.updateImageUrl(newImageUrl);
    }
}
