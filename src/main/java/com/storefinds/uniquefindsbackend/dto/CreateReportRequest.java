package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReportRequest {

    @NotBlank(message = "reasonType is required")
    private String reasonType;

    @Size(max = 500, message = "reasonDetail length must be less than or equal to 500")
    private String reasonDetail;
}
