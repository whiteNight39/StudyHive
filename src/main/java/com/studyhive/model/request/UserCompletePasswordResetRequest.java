package com.studyhive.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCompletePasswordResetRequest {

    private String otp;
    private String userEmail;
    private String userNewPassword;
}
