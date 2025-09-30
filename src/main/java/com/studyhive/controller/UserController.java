package com.studyhive.controller;

import com.studyhive.model.request.*;
import com.studyhive.model.response.BaseResponse;
import com.studyhive.service.UserService;
import com.studyhive.util.jwt.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/initiate-user-signup")
    public BaseResponse<?> initiateUserSignup(
            @Valid @RequestBody UserInitiateSignUpRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );

            BaseResponse<String> errorResponse = BaseResponse.<String>builder()
                    .responseCode("400")
                    .responseMessage("Validation failed")
                    .responseData(errors.toString())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse).getBody();
        }

        return userService.initiateUserSignup(request);
    }

    @PostMapping("/complete-user-signup")
    public BaseResponse<?> completeUserSignup(
            @Valid @RequestBody UserCompleteSignUpRequest request) {

        return userService.completeUserSignup(request);
    }

    @PostMapping("/resend-user-otp")
    public BaseResponse<?> resendUserOtp(
            @Valid @RequestParam String userEmail) {

        return userService.resendUserOtp(userEmail);
    }

    @GetMapping("/verify-unique-username")
    public BaseResponse<?> verifyUniqueUsername(
            @Valid @RequestParam String userName) {

        return userService.verifyUniqueUserName(userName);
    }

    @PostMapping("/login-user")
    public BaseResponse<?> loginUser(
            @Valid @RequestBody UserLogInRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        String ip = servletRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = servletRequest.getRemoteAddr();
        } else {
            ip = ip.split(",")[0];  // Use the first IP if multiple are present
        }
        String userAgent = servletRequest.getHeader("User-Agent");

//                String jwToken = (String) response.getResponseData();
//
//        ResponseCookie cookie = ResponseCookie.from("jwt", jwToken)
//                .httpOnly(true)
//                .secure(true)         // ✅ Change back to true for HTTPS
//                .sameSite("None")     // ✅ Change back to None for cross-site
//                .path("/")
//                .maxAge(3 * 60 * 60)
//                .build();
//
//        servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
//
//        response.setResponseData(null);

        return userService.logInUser(request, ip, userAgent);
    }

    @PostMapping("/confirm-user-location")
    public BaseResponse<?> confirmLocation(
            @Valid @RequestBody LocationConfirmRequest request) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userService.confirmLocation(request, principal.getUserId());
    }

//    @PostMapping("/login-user")
//    public ResponseEntity<BaseResponse<?>> loginUser(
//            @Valid @RequestBody UserLogInRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
//
//        String ip = servletRequest.getHeader("X-Forwarded-For");
//        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
//            ip = servletRequest.getRemoteAddr();
//        } else {
//            ip = ip.split(",")[0];  // Use the first IP if multiple are present
//        }
//        String userAgent = servletRequest.getHeader("User-Agent");
//
//        BaseResponse<?> response = userService.logInUser(request, ip, userAgent);
//
//        String jwToken = (String) response.getResponseData();
//
//        ResponseCookie cookie = ResponseCookie.from("jwt", jwToken)
//                .httpOnly(true)
//                .secure(true)         // ✅ Change back to true for HTTPS
//                .sameSite("None")     // ✅ Change back to None for cross-site
//                .path("/")
//                .maxAge(3 * 60 * 60)
//                .build();
//
//        servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
//
//        response.setResponseData(null);
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, cookie.toString())
//                .body(response);
//    }
//
    @PostMapping("/complete-user-profile")
    public BaseResponse<?> completeUserProfile(
            @Valid @RequestBody UserProfileCreateRequest request) {

        return userService.completeUserProfile(request);
    }

    @PatchMapping("/update-user-details")
    public BaseResponse<?> updateUserDetails(
            @Valid @RequestBody UserUpdateRequest request) {
        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userService.updateUserDetails(request, principal.getUserId());
    }

    @PatchMapping("/update-user-password")
    public BaseResponse<?> updateUserPassword(
            @Valid @RequestBody UserUpdateRequest request) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userService.updateUserPassword(request, principal.getUserId());
    }

    @PatchMapping("/initiate-password-reset")
    public BaseResponse<?> initiatePasswordReset(
            @Valid @RequestBody UserInitiatePasswordResetRequest request) {

        return userService.initiatePasswordReset(request);
    }

    @PatchMapping("/complete-password-reset")
    public BaseResponse<?> completePasswordReset(
            @Valid @RequestBody UserCompletePasswordResetRequest request) {

        return userService.completePasswordReset(request);
    }

    @DeleteMapping("/deactivate-user-account")
    public BaseResponse<?> deactivateUserAccount() {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userService.deactivateUserAccount(principal.getUserId());
    }

    @GetMapping("/view-user-personal-profile")
    public BaseResponse<?> viewUserPersonalProfile() {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userService.viewUserPersonalProfile(principal.getUserId());
    }

    @GetMapping("/view-user-rooms")
    public BaseResponse<?> viewUserRooms() {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userService.viewUserRooms(principal.getUserId());
    }

    @GetMapping("/search-user-profiles")
    public BaseResponse<?> searchUserProfiles(String searchQuery) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userService.searchUserProfiles(searchQuery, principal.getUserId());
    }

    @GetMapping("/view-user-global-profile/{vieweeUserId}")
    public BaseResponse<?> viewUserGlobalProfile(
            @Valid @PathVariable UUID vieweeUserId) {

        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userService.viewUserGlobalProfile(principal.getUserId(), vieweeUserId);
    }

    @GetMapping("/view-all-users")
    public BaseResponse<?> viewAllUsers(
            @RequestParam int page,
            @RequestParam int pageSize) {
        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userService.viewAllUsers(principal.getUserId(), page, pageSize);
    }
}
