package com.softkit.repository;

import com.softkit.model.Invite;
import com.softkit.model.InviteStatus;
import com.softkit.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InviteRepository extends JpaRepository<Invite, Integer> {
    boolean existsByEmail(String email);

    Invite findInviteByEmail(String email);

    boolean existsByEmailAndUser(String email, User user);

    Invite findByEmailAndUser(String email, User user);

    int deleteByEmailAndStatus(String email, InviteStatus status);

    @Query(
            value = "SELECT * FROM invitation ORDER BY status, departure_date DESC",
            nativeQuery = true)
    Page<Invite> findAll(Pageable pageable);
}
