package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.User;
import com.studyhive.model.interfaces.UserGlobalProfileProjection;
import com.studyhive.model.interfaces.UserGlobalProfileResponses;
import com.studyhive.model.interfaces.UserProfileResponses;
import com.studyhive.model.response.UserRoomProfileResponse;
import com.studyhive.model.response.UserGlobalProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> getByUserId(UUID userId);
    Optional<User> getByUserEmail(String userEmail);
    List<User> findAll();

    @Query("SELECT u FROM User u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    List<User> searchUsersByUserName(@Param("searchQuery") String searchQuery);

    @Query("SELECT u.userId AS userId, u.userName AS userName FROM User u")
    Page<UserGlobalProfileProjection> findAllGlobalProfiles(Pageable pageable);

    @Query("SELECT u, r.roomName, m.membershipRoleInRoom FROM User u LEFT JOIN Membership m ON m.membershipUser.userId = u.userId LEFT JOIN Room r ON r.roomId = m.membershipRoom.roomId WHERE u.userId = :userId")
    List<UserProfileResponses> userProfiles(@Param("userId") UUID userId);

    @Query("SELECT u, r.roomName, m.membershipRoleInRoom FROM User u LEFT JOIN Membership m ON m.membershipUser.userId = u.userId LEFT JOIN Room r ON r.roomId = m.membershipRoom.roomId WHERE u.userId = :userId AND r.roomPrivacy = 'OPEN'")
    List<UserGlobalProfileResponses> userGlobalProfiles(@Param("userId") UUID userId);

    Optional<User> getByUserName(String userName);

    boolean existsByUserName(String newUsername);
}
