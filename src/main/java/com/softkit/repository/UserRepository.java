package com.softkit.repository;

import com.softkit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByUsername(String username);

    User findByUsername(String username);

    int deleteByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    boolean existsByActivationKeyAndIsActivateFalse(String uuid);

    User findByActivationKey(String uuid);

    @Query(value = "SELECT u.id, u.username, u.first_name, u.last_name," +
            "u.birthday, u.email, u.activation_key, u.is_activate," +
            "u.user_avatar, u.registration_date, u.unconfirmed_email " +
            "FROM users AS u",
            nativeQuery = true)
    String[] findAllExceptPassword();

    User findByUnconfirmedEmail(String email);

}
