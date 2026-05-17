package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.ImageUploadResponse;
import com.storefinds.uniquefindsbackend.service.FileStorageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Inject file storage service for image upload endpoints.
     * Params:
     * - fileStorageService: local file storage business service
     * Returns: None
     * Throws: None
     */
    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/images")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Upload one image file and return its public access URL.
     * Params:
     * - file: uploaded multipart image file
     * Returns:
     * - Result<ImageUploadResponse>: uploaded image metadata
     * Throws: None
     */
    public Result<ImageUploadResponse> uploadImage(@RequestPart("file") MultipartFile file) {
        return Result.success("image uploaded", fileStorageService.storeImage(file));
    }
}
