package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Author: Enqi Guo
 * Date: 2026-05-27
 * Purpose: Capture moderator notes or reasons used when approving, rejecting, hiding, or deleting content.
 */
@Data
public class ModerationActionRequest {

    @Size(max = 255, message = "reason length must be less than or equal to 255")
    private String reason;
}
