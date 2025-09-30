package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.Room;
import com.studyhive.model.entity.User;
import com.studyhive.model.enums.RoomPrivacy;
import com.studyhive.model.interfaces.SearchRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    Optional<Room> findRoomByRoomIdAndRoomStatus(UUID roomId, String status);

    List<Room> getByRoomCreatedByAndRoomStatus(User user, String status);

    List<Room> getByRoomPrivacyAndRoomStatus(RoomPrivacy roomPrivacy, String status);

    List<Room> findByRoomPrivacyInAndRoomStatus(List<RoomPrivacy> roomPrivacies, String status);

    @Modifying
    @Query("UPDATE Room SET roomStatus = 'DELETED' WHERE roomId = :roomId")
    void deleteByRoomId(@Param("roomId") UUID roomId);

    @Query("""
    SELECT
        r.roomId as roomId,
        r.roomName as roomName,
        r.roomDescription as roomDescription,
        r.roomSize as roomSize,
        r.roomPrivacy as roomPrivacy,
        r.roomCreatedBy.userName as roomOwner,
        r.roomCreatedAt as roomCreatedAt,
        CASE WHEN m.membershipId IS NOT NULL THEN TRUE ELSE FALSE END as userIsMember
    FROM Room r
    JOIN r.roomCreatedBy u
    LEFT JOIN Membership m
      ON r.roomId = m.membershipRoom.roomId
     AND m.membershipUser.userId = :userId
     AND m.membershipStatus = 'ACTIVE'
    WHERE LOWER(r.roomName) LIKE LOWER(CONCAT('%', :searchQuery, '%'))
      AND r.roomStatus != 'DELETED'
      AND (
          r.roomPrivacy <> 'CLOSED'
          OR m.membershipId IS NOT NULL)
    """)
    List<SearchRoom> searchRoomByRoomName(
            @Param("searchQuery") String searchQuery,
            @Param("userId") UUID userId);

    @Query("""
    SELECT
        r.roomId as roomId,
        r.roomName as roomName,
        r.roomDescription as roomDescription,
        r.roomSize as roomSize,
        r.roomPrivacy as roomPrivacy,
        r.roomCreatedBy.userName as roomOwner,
        r.roomCreatedAt as roomCreatedAt
    FROM Room r
    WHERE LOWER(r.roomName)
    LIKE LOWER(CONCAT('%', :searchQuery, '%'))
    AND r.roomStatus != 'DELETED'
    """)
    List<SearchRoom> searchRoomBy_RoomName(@Param("searchQuery") String searchQuery);

}