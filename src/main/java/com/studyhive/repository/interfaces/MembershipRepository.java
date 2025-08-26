package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.Membership;
import com.studyhive.model.entity.Room;
import com.studyhive.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    List<Membership> getByMembershipRoom(Room room);

    List<Membership> getByMembershipRoomAndMembershipStatus(Room membershipRoom, String membershipStatus);
    Optional<Membership> findMembershipByMembershipRoomAndMembershipUser(Room room, User user);
}