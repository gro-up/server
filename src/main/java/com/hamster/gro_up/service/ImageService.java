package com.hamster.gro_up.service;

import com.hamster.gro_up.dto.ImageFile;
import com.hamster.gro_up.dto.response.ImageResponse;
import com.hamster.gro_up.exception.image.ImageException;
import com.hamster.gro_up.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class ImageService {
    private final ImageUtil imageUtil;

    public ImageResponse save(MultipartFile image) {
        validateSizeOfImage(image);

        ImageFile imageFile = new ImageFile(image);
        String imageUrl = imageUtil.uploadImage(imageFile);

        return new ImageResponse(imageUrl);
    }

    public void deleteImage(String originalUrl) {
        imageUtil.deleteImage(originalUrl);
    }

    private void validateSizeOfImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ImageException("이미지가 존재하지 않습니다.");
        }
    }
}
