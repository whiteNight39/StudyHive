package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.Room;
import com.studyhive.model.entity.User;
import com.studyhive.model.enums.RoomPrivacy;
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
}
