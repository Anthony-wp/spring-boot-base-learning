package com.softkit.repository;

import com.softkit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByUsername(String username);

    User findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByIdentifier(String uuid);

    User findByIdentifier(String uuid);

}
