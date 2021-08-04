package com.softkit.repository;

import com.softkit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByUsername(String username);

    User findByUsername(String username);

    void deleteByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    boolean existsByActivationKeyAndIsActivateFalse(String uuid);

    User findByActivationKey(String uuid);

}
