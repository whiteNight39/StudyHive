package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.UserLoginJwt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLoginJwtRepository extends JpaRepository<UserLoginJwt, UUID> {

    @Query("SELECT u FROM UserLoginJwt u WHERE u.user.userId = :userId AND u.userLoginJwtDeviceIp = :deviceIp AND u.userLoginJwtUserAgent = :deviceUserAgent AND u.userLoginJwtExpiresAt < CURRENT_TIMESTAMP")
    Optional<UserLoginJwt> findJwt(@Param("userId") UUID userId,
                                   @Param("deviceIp") String deviceIp,
                                   @Param("deviceUserAgent") String deviceUserAgent);

}
