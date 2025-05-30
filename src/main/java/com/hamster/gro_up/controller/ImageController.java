package com.hamster.gro_up.controller;

import com.hamster.gro_up.dto.ApiResponse;
import com.hamster.gro_up.dto.response.ImageResponse;
import com.hamster.gro_up.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "이미지", description = "이미지 관련 API")
@RequiredArgsConstructor
@RequestMapping("/api/images")
@RestController
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "S3에 이미지 업로드")
    @PostMapping
    public ResponseEntity<ApiResponse<ImageResponse>> uploadImage(@RequestPart MultipartFile image) {
        final ImageResponse imageResponse = imageService.save(image);
        return ResponseEntity.ok(ApiResponse.ok(imageResponse));
    }
}
