package com.studyhive.util.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiException extends RuntimeException {
    private String errorCode;
    private String errorMessage;
    private Object errorData; // can hold anything (string, map, DTO, etc.)

    // Forward errorMessage to RuntimeException's "message"
    @Override
    public String getMessage() {
        return errorMessage;
    }
}
