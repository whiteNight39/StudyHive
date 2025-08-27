package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserOtpRepository extends JpaRepository<UserOtp, UUID> {

    List<UserOtp> findByUserOtpUserEmailAndUserOtpReasonAndUserOtpStatusOrderByUserOtpCreatedAtDesc(
            String userEmail,
            String userOtpReason,
            String userOtpStatus
    );

    @Modifying
    @Query("UPDATE UserOtp SET userOtpStatus = 'DELETED' WHERE userOtpId = :userOtpId")
    void deleteByUserOtpId(@Param("userOtpId") UUID userOtpId);

//    void saveUserOtp(String userEmail, String otp);
}
