package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreatePostRequest {

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title must be at most 200 characters")
    private String title;

    @NotBlank(message = "description is required")
    private String description;

    private Long storeId;

    private Long categoryId;

    @DecimalMin(value = "0.00", message = "priceMin must be greater than or equal to 0")
    private BigDecimal priceMin;

    @DecimalMin(value = "0.00", message = "priceMax must be greater than or equal to 0")
    private BigDecimal priceMax;

    @Size(min = 3, max = 3, message = "currency must be 3 characters")
    private String currency;

    @Size(max = 255, message = "locationText must be at most 255 characters")
    private String locationText;

    @Valid
    private List<PostImageRequest> images;
}
