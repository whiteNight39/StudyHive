package com.studyhive.util.exception;

import com.studyhive.model.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<BaseResponse> handleApiException(ApiException ex) {
        BaseResponse response = new BaseResponse(
                ex.getErrorCode(),
                ex.getErrorMessage(), // ✅ use custom errorMessage
                ex.getErrorData()     // ✅ include errorData in response
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleGenericException(Exception ex) {
        BaseResponse response = new BaseResponse("500", "An unexpected error occurred", null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
