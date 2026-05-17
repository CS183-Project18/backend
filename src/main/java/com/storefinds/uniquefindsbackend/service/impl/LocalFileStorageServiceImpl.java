package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.config.FileStorageProperties;
import com.storefinds.uniquefindsbackend.dto.ImageUploadResponse;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-17
 * Purpose: Store uploaded images locally with stricter type and path validation.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageServiceImpl.class);
    private static final Map<String, String> CONTENT_TYPE_TO_EXTENSION = new LinkedHashMap<>();

    static {
        CONTENT_TYPE_TO_EXTENSION.put("image/jpeg", ".jpg");
        CONTENT_TYPE_TO_EXTENSION.put("image/png", ".png");
        CONTENT_TYPE_TO_EXTENSION.put("image/webp", ".webp");
        CONTENT_TYPE_TO_EXTENSION.put("image/gif", ".gif");
    }

    private final FileStorageProperties fileStorageProperties;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-17
     * Purpose: Inject local storage configuration for image upload handling.
     * Params:
     * - fileStorageProperties: local file storage settings
     * Returns: None
     * Throws: None
     */
    public LocalFileStorageServiceImpl(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-17
     * Purpose: Validate and store one uploaded image on the local filesystem.
     * Params:
     * - file: uploaded image file
     * Returns:
     * - ImageUploadResponse: uploaded image metadata and public URL
     * Throws:
     * - BusinessException: when file is empty, invalid, or storage fails
     */
    public ImageUploadResponse storeImage(MultipartFile file) {
        validateImageFile(file);

        Path imageDirPath = Paths.get(fileStorageProperties.getImageDir()).toAbsolutePath().normalize();
        String extension = getFileExtension(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + extension;
        Path targetPath = imageDirPath.resolve(storedFileName).normalize();
        if (!targetPath.startsWith(imageDirPath)) {
            throw new BusinessException("invalid image file path");
        }

        try {
            Files.createDirectories(imageDirPath);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("failed to store image: originalFilename={}, contentType={}",
                    file.getOriginalFilename(),
                    file.getContentType(),
                    ex);
            throw new BusinessException("failed to store image");
        }

        ImageUploadResponse response = new ImageUploadResponse();
        response.setFileName(storedFileName);
        response.setContentType(file.getContentType());
        response.setSize(file.getSize());
        response.setUrl(buildPublicUrl(storedFileName));
        return response;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-17
     * Purpose: Validate uploaded image content before local storage, including MIME and extension checks.
     * Params:
     * - file: uploaded image file
     * Returns: None
     * Throws:
     * - BusinessException: when file is empty, oversized, or not an allowed image type
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("image file is required");
        }
        if (file.getSize() > fileStorageProperties.getMaxImageSize()) {
            throw new BusinessException("image file is too large");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            throw new BusinessException("only jpeg, png, webp, and gif images are supported");
        }
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        if (!new LinkedHashSet<>(fileStorageProperties.getAllowedContentTypes()).contains(normalizedContentType)) {
            throw new BusinessException("only jpeg, png, webp, and gif images are supported");
        }
        validateFileExtension(file.getOriginalFilename(), normalizedContentType);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-17
     * Purpose: Build one public image access URL from the stored file name.
     * Params:
     * - storedFileName: generated stored image file name
     * Returns:
     * - String: public image URL
     * Throws: None
     */
    private String buildPublicUrl(String storedFileName) {
        return fileStorageProperties.getPublicBaseUrl().replaceAll("/+$", "") + "/uploads/images/" + storedFileName;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-17
     * Purpose: Extract one safe file extension from the original uploaded file name.
     * Params:
     * - originalFilename: raw uploaded file name
     * Returns:
     * - String: normalized file extension with leading dot or empty string
     * Throws: None
     */
    private String getFileExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        String cleanedFileName = Paths.get(originalFilename).getFileName().toString();
        int lastDotIndex = cleanedFileName.lastIndexOf('.');
        if (lastDotIndex < 0) {
            return "";
        }
        return cleanedFileName.substring(lastDotIndex).toLowerCase(Locale.ROOT);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-17
     * Purpose: Verify the uploaded file extension matches the declared content type.
     * Params:
     * - originalFilename: raw uploaded file name
     * - contentType: normalized content type
     * Returns: None
     * Throws:
     * - BusinessException: when the extension is missing or mismatched
     */
    private void validateFileExtension(String originalFilename, String contentType) {
        String extension = getFileExtension(originalFilename);
        if (!StringUtils.hasText(extension)) {
            throw new BusinessException("image file extension is required");
        }
        String expectedExtension = CONTENT_TYPE_TO_EXTENSION.get(contentType);
        if (expectedExtension == null) {
            throw new BusinessException("only jpeg, png, webp, and gif images are supported");
        }
        if ("image/jpeg".equals(contentType) && (".jpg".equals(extension) || ".jpeg".equals(extension))) {
            return;
        }
        if (!expectedExtension.equals(extension)) {
            throw new BusinessException("image file extension does not match content type");
        }
    }
}
