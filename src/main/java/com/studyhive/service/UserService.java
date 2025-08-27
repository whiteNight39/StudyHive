package com.studyhive.service;

import com.studyhive.mapper.UserMapper;
import com.studyhive.model.entity.Role;
import com.studyhive.model.entity.User;
import com.studyhive.model.entity.UserLoginJwt;
import com.studyhive.model.entity.UserOtp;
import com.studyhive.model.interfaces.UserGlobalProfileProjection;
import com.studyhive.model.interfaces.UserGlobalProfileResponses;
import com.studyhive.model.request.*;
import com.studyhive.model.response.BaseResponse;
import com.studyhive.model.response.UserGlobalProfileResponse;
import com.studyhive.model.response.UserProfileResponse;
import com.studyhive.model.interfaces.UserProfileResponses;
import com.studyhive.repository.interfaces.RoleRepository;
import com.studyhive.repository.interfaces.UserLoginJwtRepository;
import com.studyhive.repository.interfaces.UserOtpRepository;
import com.studyhive.repository.interfaces.UserRepository;
import com.studyhive.util.TokenGenerator;
import com.studyhive.util.jwt.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final EmailService emailService;

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserOtpRepository userOtpRepository;
    private final UserLoginJwtRepository userLoginJwtRepository;

    private final JwtUtil jwtUtil;

    private final RoleRepository roleRepository;

    private final String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$\n";
    private final Pattern pattern = Pattern.compile(passwordPattern);

    public UserService(EmailService emailService, UserMapper userMapper, UserRepository userRepository, UserOtpRepository userOtpRepository, UserLoginJwtRepository userLoginJwtRepository, JwtUtil jwtUtil, RoleRepository roleRepository) {
        this.emailService = emailService;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.userOtpRepository = userOtpRepository;
        this.userLoginJwtRepository = userLoginJwtRepository;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public BaseResponse initiateUserSignup(UserInitiateSignUpRequest request) {
        if (request == null)
            throw new IllegalArgumentException("request cannot be null");

        Optional<User> existingUser = userRepository.getByUserEmailAndUserStatus(request.getUserEmail(), "ACTIVE");
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("user already exists");
        }
        if (!isPasswordStrong(request.getUserPassword())) {
            throw new IllegalArgumentException("Minimum 8 characters required for password, " +
                    "at least one uppercase, one lowercase, one digit, one special character");
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        String otp = TokenGenerator.generateSecureToken(12);
        String hashedOtp = bCryptPasswordEncoder.encode(otp);
        String hashedPassword = bCryptPasswordEncoder.encode(request.getUserPassword());

        UserOtp userOtp = UserOtp.builder()
                .userOtpUserEmail(request.getUserEmail())
                .userOtpOtp(hashedOtp)
                .userOtpReason("ENROLLMENT")
                .userOtpExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        userOtpRepository.save(userOtp);

        emailService.sendEmail(request.getUserEmail(),
                emailService.MSG_ENROLLMENT_TITLE,
                String.format(emailService.MSG_ENROLLMENT_BODY, otp));

        Role defaultRole = roleRepository.getByRoleName("USER").get(0);

        User user = User.builder()
                .userEmail(request.getUserEmail())
                .userPassword(hashedPassword)
                .userStatus("PENDING")
                .userRole(defaultRole)
                .build();
        userRepository.save(user);


        return new BaseResponse("00","OTP has been sent", null);
    }

    @Transactional
    public BaseResponse completeUserSignup(UserCompleteSignUpRequest request) {
        if  (request == null)
            throw new IllegalArgumentException("request cannot be null");

        boolean foundMatchingOtp = false;
        boolean otpIsValid =  false;

        List<UserOtp> userOtps = userOtpRepository.findByUserOtpUserEmailAndUserOtpReasonAndUserOtpStatusOrderByUserOtpCreatedAtDesc(
                request.getUserEmail(),
                "ENROLLMENT",
                "ACTIVE");
        if (userOtps.isEmpty())
            throw new IllegalArgumentException("User has not initiated enrollment");
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        for (UserOtp userOtp : userOtps) {

            if (bCryptPasswordEncoder.matches(request.getOtp(), userOtp.getUserOtpOtp())) {
                foundMatchingOtp = true;

                if (Instant.now().isAfter(userOtp.getUserOtpExpiresAt())) {
                    userOtp.setUserOtpStatus("DELETED");
                    userOtpRepository.save(userOtp);
                    continue;
                }

                User user = userRepository.getByUserEmailAndUserStatus(request.getUserEmail(), "ACTIVE")
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                user.setUserStatus("ACTIVE");
                userRepository.save(user);
                otpIsValid = true;

                userOtp.setUserOtpStatus("DELETED");
                userOtpRepository.save(userOtp);
                break;
            }
        }
        if (!foundMatchingOtp)
            throw new IllegalArgumentException("Invalid OTP");
        if (!otpIsValid)
            throw new IllegalArgumentException("Otp is expired");

        return new BaseResponse("00","Account Activated", null);
    }

    public BaseResponse logInUser(UserLogInRequest request, String deviceIp, String deviceUserAgent) {
        if (request == null)
            throw new IllegalArgumentException("request cannot be null");

        Optional<User> userByEmail = userRepository.getByUserEmailAndUserStatus(request.getUserEmail(), "ACTIVE");
        Optional<User> userByUsername = userRepository.getByUserNameAndUserStatus(request.getUserName(),  "ACTIVE");

        User user = userByEmail.or(() -> userByUsername)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username and/or password"));
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        if (!bCryptPasswordEncoder.matches(request.getUserPassword(), user.getUserPassword())) {
            throw new IllegalArgumentException("Invalid username and/or password");
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


        return new BaseResponse("00","Log in successful", jwtToken);
    }

    public BaseResponse completeUserProfile(UserProfileCreateRequest request) {
        if (request == null)
            throw new IllegalArgumentException("request cannot be null");

        userRepository.getByUserNameAndUserStatus(request.getUserName(), "ACTIVE")
                .ifPresent(user -> {
                    throw new IllegalArgumentException("Username already in use");
                });

        User user = userRepository.getByUserEmailAndUserStatus(request.getUserEmail(),  "ACTIVE")
                        .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + request.getUserEmail()));

        if (user.getUserName() != null) {
            throw new IllegalStateException("Profile already created");
        }

        user.setUserName(request.getUserName());
        user.setUserFirstName(request.getUserFirstName());
        user.setUserLastName(request.getUserLastName());
        userRepository.save(user);

        return new BaseResponse("00","User profile completed successfully", null);
    }

    @Transactional
    public BaseResponse updateUserDetails(UserUpdateRequest request, UUID userId) {
        if (request == null)
            throw new IllegalArgumentException("request cannot be null");

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if username is being updated
        String newUsername = request.getUserName();
        if (newUsername != null && !newUsername.equals(user.getUserName())) {
            boolean usernameTaken = userRepository.existsByUserName(newUsername);
            if (usernameTaken) {
                throw new IllegalArgumentException("Username already in use");
            }
            user.setUserName(newUsername);
        }

        Optional.ofNullable(request.getUserFirstName()).ifPresent(user::setUserFirstName);
        Optional.ofNullable(request.getUserLastName()).ifPresent(user::setUserLastName);
        userRepository.save(user);

        return new BaseResponse("00","User updated successfully", null);
    }

    @Transactional
    public BaseResponse updateUserPassword(UserUpdateRequest request, UUID userId) {
        if (request == null)
            throw new IllegalArgumentException("request cannot be null");

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        if (!bCryptPasswordEncoder.matches(request.getUserOldPassword(), user.getUserPassword())) {
            throw new IllegalArgumentException("Old password doesn't match");
        }
        if (!isPasswordStrong(request.getUserNewPassword())) {
            throw new IllegalArgumentException("Minimum 8 characters required for password, " +
                    "at least one uppercase, one lowercase, one digit, one special character");
        }
        String hashedNewPassword = bCryptPasswordEncoder.encode(request.getUserNewPassword());
        user.setUserPassword(hashedNewPassword);
        userRepository.save(user);

        return new BaseResponse("00","User password updated successfully", null);
    }

    @Transactional
    public BaseResponse initiatePasswordReset(UserInitiatePasswordResetRequest request) {
        if (request == null)
            throw new IllegalArgumentException("request cannot be null");

        User user = userRepository.getByUserEmailAndUserStatus(request.getUserEmail(), "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
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

        emailService.sendEmail(request.getUserEmail(),
                emailService.MSG_PASSWORD_RESET_TITLE,
                String.format(emailService.MSG_PASSWORD_RESET_BODY, otp));

        return new BaseResponse("00","Password reset OTP sent", null);
    }

    @Transactional
    public BaseResponse completePasswordReset(UserCompletePasswordResetRequest request) {

        if (request == null)
            throw new IllegalArgumentException("request cannot be null");

        List<UserOtp> userOtps = userOtpRepository.findByUserOtpUserEmailAndUserOtpReasonAndUserOtpStatusOrderByUserOtpCreatedAtDesc(
                request.getUserEmail(),
                "PASSWORD_RESET",
                "ACTIVE");
        if (userOtps.isEmpty())
            throw new IllegalArgumentException("User has not initiated password reset");
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
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
                user.setUserPassword(hashedNewPassword);
                userRepository.save(user);
                otpIsValid = true;

                userOtp.setUserOtpStatus("DELETED");
                userOtpRepository.save(userOtp);
                break;
            }
        }
        if (!foundMatchingOtp)
            throw new IllegalArgumentException("Invalid OTP");
        if (!otpIsValid)
            throw new IllegalArgumentException("Otp is expired");

        return new BaseResponse("00","Password reset successful", null);
    }

    public BaseResponse deactivateUserAccount(UUID userId) {

        if (userId == null)
            throw new IllegalArgumentException("userId cannot be null");

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userRepository.deleteByUserId(userId);

        return new BaseResponse("00","User deactivated successfully", null);
    }

    public BaseResponse viewUserPersonalProfile(UUID userId) {
        if (userId == null)
            throw new IllegalArgumentException("userId cannot be null");

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<UserProfileResponses> responses = userRepository.userProfiles(userId);
        UserProfileResponse userProfileResponse = UserProfileResponse.builder()
                .userId(responses.get(0).getUserId())
                .userName(responses.get(0).getUserName())
                .userFirstName(responses.get(0).getUserFirstName())
                .userLastName(responses.get(0).getUserLastName())
                .userEmail(responses.get(0).getUserEmail())
                .userMaxRooms(responses.get(0).getUserMaxRooms())
                .userCreditScore(responses.get(0).getUserCreditScore())
                .userRoomInfo(responses.stream()
                        .filter(r -> r.getUserRoomsRoomName() != null && r.getUserRoomsRoomRole() != null)
                        .map(r -> new UserProfileResponse.UserRoomInfo(r.getUserRoomsRoomName(), r.getUserRoomsRoomRole()))
                        .toList())
                .build();

        return new BaseResponse("00","User personal profile", userProfileResponse);
    }

    public BaseResponse searchUserProfiles(String searchQuery, UUID userId) {
        if (searchQuery == null)
            throw new IllegalArgumentException("searchQuery cannot be null");

        User user = userRepository.getByUserIdAndUserStatus(userId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<User> users = userRepository.searchUsersByUserName(searchQuery);

        List<UserGlobalProfileResponse> responses = users.stream()
                .map(userMapper::toGlobalProfileResponse)
                .toList();

        return new BaseResponse("00", "Search results", responses);
    }

    public BaseResponse viewUserGlobalProfile(UUID viewerUserId, UUID vieweeUserId) {
        if (viewerUserId == null) {
            throw new IllegalArgumentException("viewerUserId cannot be null");
        }
        if (vieweeUserId == null) {
            throw new IllegalArgumentException("vieweeUserId cannot be null");
        }

        // Only fetch viewer if necessary (for permissions)
        userRepository.getByUserIdAndUserStatus(viewerUserId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("Viewer not found"));
        User vieweeUser = userRepository.getByUserIdAndUserStatus(vieweeUserId,  "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("User to view not found"));

        List<UserGlobalProfileResponses> responses = userRepository.userGlobalProfiles(vieweeUserId);
        UserGlobalProfileResponse response = UserGlobalProfileResponse.builder()
                .userId(responses.get(0).getUserId())
                .userName(responses.get(0).getUserName())
                .userRoomInfo(responses.stream()
                        .filter(r -> r.getUserRoomsRoomName() != null && r.getUserRoomsRoomRole() != null)
                        .map(r -> new UserGlobalProfileResponse.UserRoomInfo(r.getUserRoomsRoomName(), r.getUserRoomsRoomRole()))
                        .toList())
                .build();

        return new BaseResponse("00", "User global profile", response);
    }

    public BaseResponse viewAllUsers(UUID userId, int page, int size) {
        if (userId == null)
            throw new IllegalArgumentException("userId cannot be null");

        Pageable pageable = PageRequest.of(page, size, Sort.by("userName").ascending());
        Page<UserGlobalProfileProjection> usersPage = userRepository.findAllGlobalProfiles(pageable);

        return new BaseResponse(
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
