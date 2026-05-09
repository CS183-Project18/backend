package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ModerationActionRequest {

    @Size(max = 255, message = "reason length must be less than or equal to 255")
    private String reason;
}
