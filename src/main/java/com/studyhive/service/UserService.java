package com.studyhive.service;

import com.studyhive.mapper.UserMapper;
import com.studyhive.model.entity.Role;
import com.studyhive.model.entity.User;
import com.studyhive.model.entity.UserLoginJwt;
import com.studyhive.model.entity.UserOtp;
import com.studyhive.model.interfaces.SearchUser;
import com.studyhive.model.interfaces.UserGlobalProfileProjection;
import com.studyhive.model.interfaces.UserGlobalProfileResponses;
import com.studyhive.model.request.*;
import com.studyhive.model.response.BaseResponse;
import com.studyhive.model.response.UserGlobalProfileResponse;
import com.studyhive.model.response.UserProfileResponse;
import com.studyhive.model.interfaces.UserProfileResponses;
import com.studyhive.model.response.UserRoomResponse;
import com.studyhive.repository.interfaces.RoleRepository;
import com.studyhive.repository.interfaces.UserLoginJwtRepository;
import com.studyhive.repository.interfaces.UserOtpRepository;
import com.studyhive.repository.interfaces.UserRepository;
import com.studyhive.util.GeoUtils;
import com.studyhive.util.TokenGenerator;
import com.studyhive.util.exception.ApiException;
import com.studyhive.util.jwt.JwtUtil;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private final EmailService emailService;
//    private final ResendService resendService;
    private final MailGunService mailGunService;

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserOtpRepository userOtpRepository;
    private final UserLoginJwtRepository userLoginJwtRepository;

    private final JwtUtil jwtUtil;

    private final RoleRepository roleRepository;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private final Pattern pattern = Pattern.compile(passwordPattern);

    public UserService(EmailService emailService, ResendService resendService, MailGunService mailGunService, UserMapper userMapper, UserRepository userRepository, UserOtpRepository userOtpRepository, UserLoginJwtRepository userLoginJwtRepository, JwtUtil jwtUtil, RoleRepository roleRepository) {
        this.emailService = emailService;
//        this.resendService = resendService;
        this.mailGunService = mailGunService;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.userOtpRepository = userOtpRepository;
        this.userLoginJwtRepository = userLoginJwtRepository;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public BaseResponse<?> initiateUserSignup(UserInitiateSignUpRequest request) {
        if (request == null)
            throw new ApiException("11", "User signup request cannot be null", null);

        userRepository.getByUserEmailAndUserStatus(request.getUserEmail(), "ACTIVE")
                .ifPresent(user -> {
                    throw new ApiException("55", "Email is already in use", null);
                });
        System.out.println("Email is valid");

        if (!isPasswordStrong(request.getUserPassword())) {
            throw new ApiException("11",
                    "Password must be at least 8 characters, " +
                            "and include uppercase, lowercase, digit, and special character",
                    null);
        }
        System.out.println("Password is valid");
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        String otp = TokenGenerator.generateSecureToken(12);
        String hashedOtp = bCryptPasswordEncoder.encode(otp);
        String hashedPassword = bCryptPasswordEncoder.encode(request.getUserPassword());
        System.out.println("Hashed OTP is " + hashedOtp);


        UserOtp userOtp = UserOtp.builder()
                .userOtpUserEmail(request.getUserEmail())
                .userOtpOtp(hashedOtp)
                .userOtpReason("ENROLLMENT")
                .userOtpExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        userOtpRepository.save(userOtp);
        System.out.println("User Otp has been saved");

        mailGunService.sendEnrollmentEmail(request.getUserEmail(), otp);
        System.out.println("User Otp has been sent to send email");

        Role defaultRole = roleRepository.getByRoleName("USER").get(0);

        Optional<User> existingUser = userRepository.getByUserEmailAndUserStatus(request.getUserEmail(), "PENDING");
        if (existingUser.isEmpty()) {
            User user = User.builder()
                    .userEmail(request.getUserEmail())
                    .userPassword(hashedPassword)
                    .userStatus("PENDING")
                    .userRole(defaultRole)
                    .build();
            userRepository.save(user);
            System.out.println("User has been saved");
        }

        return new BaseResponse<>("00","OTP has been sent", null);
    }

    public BaseResponse<?> resendUserOtp(String userEmail) {
        if (userEmail == null)
            throw new ApiException("11", "User email is required", null);

        userRepository.getByUserEmailAndUserStatus(userEmail, "ACTIVE")
                .ifPresent(user -> {
                    throw new ApiException("55", "Email is already in use", null);
                });

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        String otp = TokenGenerator.generateSecureToken(12);
        String hashedOtp = bCryptPasswordEncoder.encode(otp);
        System.out.println("Hashed OTP is " + hashedOtp);

        UserOtp userOtp = UserOtp.builder()
                .userOtpUserEmail(userEmail)
                .userOtpOtp(hashedOtp)
                .userOtpReason("ENROLLMENT")
                .userOtpExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        userOtpRepository.save(userOtp);
        System.out.println("User Otp has been saved");

        mailGunService.sendEnrollmentEmail(userEmail, otp);
        System.out.println("User Otp has been sent to send email");

        return new BaseResponse<>("00","OTP has been sent", null);
    }

    public BaseResponse<?> verifyUniqueUserName(String userName) {
        if (userName == null)
            throw new ApiException("11", "Username is required", null);

        boolean exists = userRepository.existsByUserName(userName);

        if (!exists) {
            return new BaseResponse<>("00", "Username is valid", null);
        } else {
            return new BaseResponse<>("55", "Username is already in use", null);
        }
    }

    @Transactional
    public BaseResponse<?> completeUserSignup(UserCompleteSignUpRequest request) {
        if  (request == null)
            throw new ApiException("11", "User signup request cannot be null", null);

        boolean foundMatchingOtp = false;
        boolean otpIsValid =  false;

        List<UserOtp> userOtps = userOtpRepository.findByUserOtpUserEmailAndUserOtpReasonAndUserOtpStatusOrderByUserOtpCreatedAtDesc(
                request.getUserEmail(),
                "ENROLLMENT",
                "ACTIVE");
        if (userOtps.isEmpty()) {
            throw new ApiException("11", "User has not initiated enrollment. OTP not found.", null);
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        for (UserOtp userOtp : userOtps) {

            if (bCryptPasswordEncoder.matches(request.getOtp(), userOtp.getUserOtpOtp())) {
                foundMatchingOtp = true;

                if (Instant.now().isAfter(userOtp.getUserOtpExpiresAt())) {
                    userOtp.setUserOtpStatus("DELETED");
                    userOtpRepository.save(userOtp);
                    continue;
                }

                User user = userRepository.getByUserEmailAndUserStatus(request.getUserEmail(), "PENDING")
                                .orElseThrow(() -> new ApiException("44", "User not found", null));
                user.setUserStatus("ACTIVE");
                userRepository.save(user);
                otpIsValid = true;

                userOtp.setUserOtpStatus("DELETED");
                userOtpRepository.save(userOtp);
                break;
            }
        }
        if (!foundMatchingOtp) {
            throw new ApiException("11", "Invalid OTP provided", null);
        }

        if (!otpIsValid) {
            throw new ApiException("11", "OTP has expired", null);
        }

        return new BaseResponse<>("00","Account Activated", null);
    }

    public BaseResponse<?> logInUser(UserLogInRequest request, String deviceIp, String deviceUserAgent) {
        if (request == null) {
            throw new ApiException("11", "Login request cannot be null", null);
        }

        Optional<User> userByEmail = userRepository.getByUserEmailAndUserStatus(request.getUserEmail(), "ACTIVE");
        Optional<User> userByUsername = userRepository.getByUserNameAndUserStatus(request.getUserName(), "ACTIVE");

        User user = userByEmail.or(() -> userByUsername)
                .orElseThrow(() -> new ApiException("11", "Invalid username and/or password", null));

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (!bCryptPasswordEncoder.matches(request.getUserPassword(), user.getUserPassword())) {
            throw new ApiException("11", "Invalid username and/or password", null);
        }

        Optional<UserLoginJwt> existingJwt = userLoginJwtRepository.findJwt(user.getUserId(), deviceIp, deviceUserAgent);
        String jwtToken;

        if (existingJwt.isEmpty()) {
            jwtToken = jwtUtil.generateToken(user.getUserId());

            UserLoginJwt newJwt = UserLoginJwt.builder()
                    .user(user)
                    .userLoginJwtToken(jwtToken)
                    .userLoginJwtIssuedAt(jwtUtil.getIssueDate(jwtToken).toInstant())
                    .userLoginJwtExpiresAt(jwtUtil.getExpirationDate(jwtToken).toInstant())
                    .userLoginJwtDeviceIp(deviceIp)
                    .userLoginJwtUserAgent(deviceUserAgent)
                    .build();

            userLoginJwtRepository.save(newJwt);
        } else {
            jwtToken = existingJwt.get().getUserLoginJwtToken();
        }

        return new BaseResponse<>("00", "Log in successful", jwtToken);
    }

    @Data
    @Builder
    static class LocationInfo {
        public Double distanceFromClass;   // meters
        public Double threshold;           // meters
        public Boolean locationVerified;   // true/false
    }

    public BaseResponse<?> confirmLocation(LocationConfirmRequest request, UUID userID) {

        User user = userRepository.getByUserIdAndUserStatus(userID, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        // 🔹 Location check setup
        double fixedLat = request.getFixedLatitude();
        double fixedLon = request.getFixedLongitude();
        double threshold = 200.0; // meters

        double userLat = request.getCurrentLatitude();
        double userLon = request.getCurrentLongitude();

        double distance = GeoUtils.calculateDistance(fixedLat, fixedLon, userLat, userLon);

        LocationInfo locationInfo = LocationInfo.builder()
                .threshold(threshold)
                .distanceFromClass(distance)
                .build();

        log.info("""
        Login location check:
          User [{}]
          Current Position:    lat={}, lon={}
          Lecture Location:    lat={}, lon={}
          Distance:            {} meters
          Allowed Threshold:   {} meters
        """,
                user.getUserEmail(),
                userLat, userLon,
                fixedLat, fixedLon,
                distance, threshold
        );

        // 🔹 Timestamp freshness check
        Duration duration = Duration.between(request.getTimeStampLocationCheck(), request.getTimeStampLoggedIn());
        if (duration.toMinutes() > 5) {
            locationInfo.setLocationVerified(false);
            return new BaseResponse<>("66", "You have passed the time limit to check in", locationInfo);
        }

        // 🔹 Distance check
        if (distance > threshold) {
            locationInfo.setLocationVerified(false);
            return new BaseResponse<>("77", "You are too far from the required location", locationInfo);
        }

        // ✅ If all good
        locationInfo.setLocationVerified(true);

        return new BaseResponse<>("00", "Location confirmation result", locationInfo);
    }

    public BaseResponse<?> completeUserProfile(UserProfileCreateRequest request) {
        if (request == null) {
            throw new ApiException("11", "Profile creation request cannot be null", null);
        }

        // Check if username is already taken
        userRepository.getByUserNameAndUserStatus(request.getUserName(), "ACTIVE")
                .ifPresent(user -> {
                    throw new ApiException("55", "Username already in use", null);
                });

        // Fetch user by email
        User user = userRepository.getByUserEmailAndUserStatus(request.getUserEmail(), "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found with email: " + request.getUserEmail(), null));

        // Ensure profile hasn't been created already
        if (user.getUserName() != null) {
            throw new ApiException("22", "User profile has already been created", null);
        }

        // Complete profile
        user.setUserName(request.getUserName());
        user.setUserFirstName(request.getUserFirstName());
        user.setUserLastName(request.getUserLastName());
        userRepository.save(user);

        return new BaseResponse<>("00", "User profile completed successfully", null);
    }

    @Transactional
    public BaseResponse<?> updateUserDetails(UserUpdateRequest request, UUID userId) {
        if (request == null) {
            throw new ApiException("11", "Update request cannot be null", null);
        }

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        // Check if username is being updated
        String newUsername = request.getUserName();
        if (newUsername != null && !newUsername.equals(user.getUserName())) {
            boolean usernameTaken = userRepository.existsByUserName(newUsername);
            if (usernameTaken) {
                throw new ApiException("55", "Username already in use", null);
            }
            user.setUserName(newUsername);
        }

        Optional.ofNullable(request.getUserFirstName()).ifPresent(user::setUserFirstName);
        Optional.ofNullable(request.getUserLastName()).ifPresent(user::setUserLastName);
        userRepository.save(user);

        return new BaseResponse<>("00", "User updated successfully", null);
    }

    @Transactional
    public BaseResponse<?> updateUserPassword(UserUpdateRequest request, UUID userId) {
        if (request == null) {
            throw new ApiException("11", "Password update request cannot be null", null);
        }

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        if (!bCryptPasswordEncoder.matches(request.getUserOldPassword(), user.getUserPassword())) {
            throw new ApiException("11", "Old password does not match", null);
        }

        if (!isPasswordStrong(request.getUserNewPassword())) {
            throw new ApiException("11",
                    "New password must be at least 8 characters and include uppercase, lowercase, digit, and special character",
                    null);
        }

        String hashedNewPassword = bCryptPasswordEncoder.encode(request.getUserNewPassword());
        user.setUserPassword(hashedNewPassword);
        userRepository.save(user);

        return new BaseResponse<>("00", "User password updated successfully", null);
    }


    @Transactional
    public BaseResponse<?> initiatePasswordReset(UserInitiatePasswordResetRequest request) {
        if (request == null) {
            throw new ApiException("11", "Password reset request cannot be null", null);
        }

        User user = userRepository.getByUserEmailAndUserStatus(request.getUserEmail(), "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        String otp = TokenGenerator.generateSecureToken(12);
        String hashedOtp = bCryptPasswordEncoder.encode(otp);

        UserOtp userOtp = UserOtp.builder()
                .userOtpUserEmail(request.getUserEmail())
                .userOtpOtp(hashedOtp)
                .userOtpReason("PASSWORD_RESET")
                .userOtpExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        userOtpRepository.save(userOtp);

        mailGunService.sendPasswordResetEmail(request.getUserEmail(), otp);

        return new BaseResponse<>("00", "Password reset OTP sent", null);
    }


    @Transactional
    public BaseResponse<?> completePasswordReset(UserCompletePasswordResetRequest request) {
        if (request == null) {
            throw new ApiException("11", "Password reset request cannot be null", null);
        }

        List<UserOtp> userOtps = userOtpRepository.findByUserOtpUserEmailAndUserOtpReasonAndUserOtpStatusOrderByUserOtpCreatedAtDesc(
                request.getUserEmail(),
                "PASSWORD_RESET",
                "ACTIVE");

        if (userOtps.isEmpty()) {
            throw new ApiException("11", "User has not initiated password reset", null);
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        boolean foundMatchingOtp = false;
        boolean otpIsValid = false;
        String hashedNewPassword = bCryptPasswordEncoder.encode(request.getUserNewPassword());

        for (UserOtp userOtp : userOtps) {
            if (bCryptPasswordEncoder.matches(request.getOtp(), userOtp.getUserOtpOtp())) {
                foundMatchingOtp = true;

                if (Instant.now().isAfter(userOtp.getUserOtpExpiresAt())) {
                    userOtp.setUserOtpStatus("DELETED");
                    userOtpRepository.save(userOtp);
                    continue;
                }

                User user = userRepository.getByUserEmailAndUserStatus(request.getUserEmail(), "ACTIVE")
                        .orElseThrow(() -> new ApiException("44", "User not found", null));

                user.setUserPassword(hashedNewPassword);
                userRepository.save(user);
                otpIsValid = true;

                userOtp.setUserOtpStatus("DELETED");
                userOtpRepository.save(userOtp);
                break;
            }
        }

        if (!foundMatchingOtp) {
            throw new ApiException("11", "Invalid OTP", null);
        }

        if (!otpIsValid) {
            throw new ApiException("11", "OTP has expired", null);
        }

        return new BaseResponse<>("00", "Password reset successful", null);
    }

    public BaseResponse<?> deactivateUserAccount(UUID userId) {
        if (userId == null) {
            throw new ApiException("11", "User ID cannot be null", null);
        }

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        userRepository.deleteByUserId(userId);

        return new BaseResponse<>("00", "User deactivated successfully", null);
    }

    public BaseResponse<?> viewUserPersonalProfile(UUID userId) {
        if (userId == null) {
            throw new ApiException("11", "User ID cannot be null", null);
        }

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        System.out.println(userId);
        List<UserProfileResponses> responses = userRepository.userProfiles(userId);
        System.out.println(responses);

        UserProfileResponse userProfileResponse = UserProfileResponse.builder()
                .userId(responses.getFirst().getUserId())
                .userName(responses.getFirst().getUserName())
                .userFirstName(responses.getFirst().getUserFirstName())
                .userLastName(responses.getFirst().getUserLastName())
                .userEmail(responses.getFirst().getUserEmail())
                .userMaxRooms(responses.getFirst().getUserMaxRooms())
                .userCreditScore(responses.getFirst().getUserCreditScore())
                .userRoomInfo(responses.stream()
                        .filter(r -> r.getUserRoomsRoomName() != null && r.getUserRoomsRoomRole() != null)
                        .map(r -> new UserProfileResponse.UserRoomInfo(r.getUserRoomsRoomName(), r.getUserRoomsRoomRole()))
                        .toList())
                .build();

        return new BaseResponse<>("00", "User personal profile", userProfileResponse);
    }

    public BaseResponse<?> viewUserRooms(UUID userId) {

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        List<UserRoomResponse> userRoomResponses = userRepository.userRooms(userId);

        return new BaseResponse<>("00", "User rooms", userRoomResponses);
    }

    public BaseResponse<?> searchUserProfiles(String searchQuery, UUID userId) {
        if (searchQuery == null) {
            throw new ApiException("11", "Search query cannot be null", null);
        }

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User not found", null));

        List<SearchUser> users = userRepository.searchUsersByUserName(searchQuery);

//        List<UserGlobalProfileResponse> responses = users.stream()
//                .map(userMapper::toGlobalProfileResponse)
//                .toList();

        return new BaseResponse<>("00", "Search results", users);
    }

    public BaseResponse<?> viewUserGlobalProfile(UUID viewerUserId, UUID vieweeUserId) {
        if (viewerUserId == null) {
            throw new ApiException("11", "Viewer user ID cannot be null", null);
        }
        if (vieweeUserId == null) {
            throw new ApiException("11", "Viewee user ID cannot be null", null);
        }

        // Fetch viewer for permissions
        userRepository.getByUserIdAndUserStatus(viewerUserId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "Viewer not found", null));

        User vieweeUser = userRepository.getByUserIdAndUserStatus(vieweeUserId, "ACTIVE")
                .orElseThrow(() -> new ApiException("44", "User to view not found", null));

        List<UserGlobalProfileResponses> responses = userRepository.userGlobalProfiles(vieweeUserId);

        UserGlobalProfileResponse response = UserGlobalProfileResponse.builder()
                .userId(responses.get(0).getUserId())
                .userName(responses.get(0).getUserName())
                .userRoomInfo(responses.stream()
                        .filter(r -> r.getUserRoomsRoomName() != null && r.getUserRoomsRoomRole() != null)
                        .map(r -> new UserGlobalProfileResponse.UserRoomInfo(r.getUserRoomsRoomName(), r.getUserRoomsRoomRole()))
                        .toList())
                .build();

        return new BaseResponse<>("00", "User global profile", response);
    }

    public BaseResponse<?> viewAllUsers(UUID userId, int page, int size) {
        if (userId == null) {
            throw new ApiException("11", "User ID cannot be null", null);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("userName").ascending());
        Page<UserGlobalProfileProjection> usersPage = userRepository.findAllGlobalProfiles(pageable);

        return new BaseResponse<>(
                "00",
                "All users",
                Map.of(
                        "content", usersPage.getContent(),
                        "currentPage", usersPage.getNumber(),
                        "totalPages", usersPage.getTotalPages(),
                        "totalElements", usersPage.getTotalElements(),
                        "pageSize", usersPage.getSize()
                )
        );
    }

    public Boolean isPasswordStrong(String password) {
        if (password == null) return false;
        return pattern.matcher(password).matches();
    }
}
