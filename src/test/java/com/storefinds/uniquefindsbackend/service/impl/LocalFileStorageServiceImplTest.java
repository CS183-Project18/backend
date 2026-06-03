package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.config.FileStorageProperties;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Verify local image storage and metadata extraction behavior.
 */
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
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", jpegBytes(20, 12));

        var response = service.storeImage(file);

        assertTrue(response.getUrl().startsWith("https://example.com/uploads/images/"));
        assertEquals(response.getUrl(), response.getThumbnailUrl());
        assertEquals(20, response.getWidth());
        assertEquals(12, response.getHeight());
        assertEquals((long) file.getSize(), response.getSize());
    }

    private byte[] jpegBytes(int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private FileStorageProperties properties() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setImageDir(tempDir.resolve("images").toString());
        properties.setPublicBaseUrl("https://example.com");
        properties.setAllowedContentTypes(List.of("image/jpeg", "image/png", "image/webp", "image/gif"));
        properties.setMaxImageSize(10000);
        return properties;
    }
}
