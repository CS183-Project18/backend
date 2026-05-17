package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.config.FileStorageProperties;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalFileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsMismatchedImageExtension() {
        LocalFileStorageServiceImpl service = new LocalFileStorageServiceImpl(properties());
        MockMultipartFile file = new MockMultipartFile("file", "bad.png", "image/jpeg", new byte[]{1, 2, 3});

        BusinessException ex = assertThrows(BusinessException.class, () -> service.storeImage(file));
        assertEquals("image file extension does not match content type", ex.getMessage());
    }

    @Test
    void storesValidImageAndBuildsPublicUrl() {
        LocalFileStorageServiceImpl service = new LocalFileStorageServiceImpl(properties());
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        String url = service.storeImage(file).getUrl();

        org.junit.jupiter.api.Assertions.assertTrue(url.startsWith("https://example.com/uploads/images/"));
    }

    private FileStorageProperties properties() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setImageDir(tempDir.resolve("images").toString());
        properties.setPublicBaseUrl("https://example.com");
        properties.setAllowedContentTypes(List.of("image/jpeg", "image/png", "image/webp", "image/gif"));
        properties.setMaxImageSize(10);
        return properties;
    }
}
