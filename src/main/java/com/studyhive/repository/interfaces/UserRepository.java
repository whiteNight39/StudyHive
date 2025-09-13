package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.User;
import com.studyhive.model.interfaces.SearchUser;
import com.studyhive.model.interfaces.UserGlobalProfileProjection;
import com.studyhive.model.interfaces.UserGlobalProfileResponses;
import com.studyhive.model.interfaces.UserProfileResponses;
import com.studyhive.model.response.UserRoomProfileResponse;
import com.studyhive.model.response.UserGlobalProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> getByUserIdAndUserStatus(UUID userId, String userStatus);

    Optional<User> getByUserEmailAndUserStatus(String userEmail, String userStatus);

    List<User> findAllByUserStatus(String userStatus);

    @Query("SELECT u.userName as userName FROM User u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :searchQuery, '%')) AND u.userStatus != 'DELETED'")
    List<SearchUser> searchUsersByUserName(@Param("searchQuery") String searchQuery);

    @Query("SELECT u.userId AS userId, u.userName AS userName FROM User u WHERE u.userStatus != 'DELETED'")
    Page<UserGlobalProfileProjection> findAllGlobalProfiles(Pageable pageable);

    @Query("""
    SELECT
        u.userId AS userId,
        u.userName AS userName,
        u.userFirstName AS userFirstName,
        u.userLastName AS userLastName,
        u.userEmail AS userEmail,
        u.userMaxRooms AS userMaxRooms,
        u.userCreditScore AS userCreditScore,
        r.roomName AS userRoomsRoomName,
        m.membershipRoleInRoom AS userRoomsRoomRole
    FROM User u
    LEFT JOIN Membership m ON m.membershipUser.userId = u.userId
    LEFT JOIN Room r ON r.roomId = m.membershipRoom.roomId
    WHERE u.userId = :userId
    AND u.userStatus <> 'DELETED'
    """)
    List<UserProfileResponses> userProfiles(@Param("userId") UUID userId);


    @Query("""
    SELECT
        u.userId       AS userId,
        u.userName     AS userName,
        u.userFirstName AS userFirstName,
        u.userLastName AS userLastName,
        u.userEmail    AS userEmail,
        u.userMaxRooms AS userMaxRooms,
        u.userCreditScore AS userCreditScore,
        r.roomName     AS roomName,
        m.membershipRoleInRoom AS membershipRoleInRoom
    FROM User u
    LEFT JOIN Membership m
        ON m.membershipUser.userId = u.userId
    LEFT JOIN Room r
        ON r.roomId = m.membershipRoom.roomId
       AND r.roomPrivacy <> 'CLOSED'
       AND r.roomPrivacy <> 'INVITE_ONLY'
    WHERE u.userId = :userId
      AND u.userStatus <> 'DELETED'
    """)
    List<UserGlobalProfileResponses> userGlobalProfiles(@Param("userId") UUID userId);

    Optional<User> getByUserNameAndUserStatus(String userName, String userStatus);

    boolean existsByUserName(String newUsername);

    @Modifying
    @Query("UPDATE User SET userStatus = 'DELETED' WHERE userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
