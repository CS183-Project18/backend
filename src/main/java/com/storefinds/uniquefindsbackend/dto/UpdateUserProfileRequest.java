package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserProfileRequest {

    @Size(max = 80, message = "nickname length must be less than or equal to 80")
    private String nickname;

    @Size(max = 500, message = "avatarUrl length must be less than or equal to 500")
    private String avatarUrl;

    @Size(max = 500, message = "bio length must be less than or equal to 500")
    private String bio;
}
