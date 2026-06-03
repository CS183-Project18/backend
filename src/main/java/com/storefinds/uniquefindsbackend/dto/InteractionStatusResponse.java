package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

/**
 * Author: Enqi Guo
 * Date: 2026-05-27
 * Purpose: Return the current like or favorite status for one interaction target.
 */
@Data
public class InteractionStatusResponse {
    private Long postId;
    private Boolean liked;
    private Boolean favorited;
}
