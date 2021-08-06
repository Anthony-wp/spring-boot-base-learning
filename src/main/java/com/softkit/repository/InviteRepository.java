package com.softkit.repository;

import com.softkit.model.Invite;
import com.softkit.model.InviteStatus;
import com.softkit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteRepository extends JpaRepository<Invite, Integer> {
    boolean existsByEmail(String email);

    Invite findInviteByEmail(String email);

    boolean existsByEmailAndUser(String email, User user);

    Invite findByEmailAndUser(String email, User user);

    int deleteByEmailAndStatus(String email, InviteStatus status);
}
