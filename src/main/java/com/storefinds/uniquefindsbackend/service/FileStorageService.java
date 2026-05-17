package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.dto.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    ImageUploadResponse storeImage(MultipartFile file);
}
