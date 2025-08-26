package com.studyhive.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLogInRequest {

    private String userEmail;
    private String userName;
    private String userPassword;
}
