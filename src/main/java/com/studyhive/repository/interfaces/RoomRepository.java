package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.Room;
import com.studyhive.model.entity.User;
import com.studyhive.model.enums.RoomPrivacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    Optional<Room> findRoomByRoomId(UUID roomId);
    List<Room> getByRoomCreatedBy(User user);

    List<Room> getByRoomPrivacy(RoomPrivacy roomPrivacy);
    List<Room> findByRoomPrivacyIn(List<RoomPrivacy> roomPrivacies);
}
