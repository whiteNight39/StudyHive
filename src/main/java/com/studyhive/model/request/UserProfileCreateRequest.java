package com.studyhive.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileCreateRequest {

    private String userName;
    private String userFirstName;
    private String userLastName;
    private String userEmail;
}
