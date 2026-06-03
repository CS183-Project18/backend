package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.dto.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Describe the storage operations used by the backend upload and image-serving module.
 */
public interface FileStorageService {

    ImageUploadResponse storeImage(MultipartFile file);
}
