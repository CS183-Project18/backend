package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {
    private Long parentId;

    @Size(max = 1000, message = "content length must be less than or equal to 1000")
    private String content;
}
