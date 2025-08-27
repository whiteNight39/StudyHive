package com.studyhive.repository.interfaces;

import com.studyhive.model.entity.Membership;
import com.studyhive.model.entity.Room;
import com.studyhive.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    List<Membership> getByMembershipRoomAndMembershipStatus(Room membershipRoom, String membershipStatus);

    Optional<Membership> findMembershipByMembershipRoomAndMembershipUserAndMembershipStatus(
            Room membershipRoom,
            User membershipUser,
            String membershipStatus);

    @Modifying
    @Query("UPDATE Membership m SET m.membershipStatus = 'DELETED' WHERE m.membershipId IN :membershipIds")
    void deleteAllByMembershipId(@Param("membershipIds") List<UUID> membershipIds);
}