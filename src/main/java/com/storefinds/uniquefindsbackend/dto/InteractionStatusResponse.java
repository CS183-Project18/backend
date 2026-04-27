package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

@Data
public class InteractionStatusResponse {
    private Long postId;
    private Boolean liked;
    private Boolean favorited;
}
