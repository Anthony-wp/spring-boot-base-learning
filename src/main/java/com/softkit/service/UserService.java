package com.softkit.service;

import com.softkit.exception.CustomException;
import com.softkit.model.User;
import com.softkit.repository.UserRepository;
import com.softkit.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final String activateUrl = "http://localhost:8080/users/activation?uuid=";

    public String signin(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            if (!userRepository.findByUsername(username).isActivate()){
                throw new CustomException("Account is not activated", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            return jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRoles());
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public String signup(User user) {
        if (!userRepository.existsByUsername(user.getUsername().toLowerCase())) {
            if (userRepository.existsByEmail(user.getEmail().toLowerCase())){
                throw new CustomException("Email is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            List<User> users = userRepository.findAll();
            for (User us : users){
                if (us.getUsername().toLowerCase().equals(user.getUsername().toLowerCase())){
                    throw new CustomException("Username is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
                }
            }

            UUID uuid = UUID.randomUUID();
            user.setIdentifier(uuid.toString());
//            emailService.sendMail(user.getEmail(), activateUrl + uuid);
            user.setEmail(user.getEmail().toLowerCase());
//            user.setUsername(user.getUsername().toLowerCase());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);

            return jwtTokenProvider.createToken(user.getUsername(), user.getRoles());
        } else {
            throw new CustomException("Username is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public User whoami(HttpServletRequest req) {
        return userRepository.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
    }

    //  method must delete user, by username, throw appropriate exception is user doesn't exists
    public void delete(String username) {
        if (userRepository.existsByUsername(username)){
            User user = userRepository.findByUsername(username);
            userRepository.delete(user);
        } else {
            throw new CustomException("User doesn't exists", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    //  method must search user, by username, throw appropriate exception is user doesn't exists
    public User search(String username) {
        if (userRepository.existsByUsername(username)){
            return userRepository.findByUsername(username);
        } else {
            throw new CustomException("User doesn't exists", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

//  method must create a new access token, similar to login
    public String refresh(String username) {
        if (userRepository.existsByUsername(username)) {
            return jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRoles());
        } else {
            throw new CustomException("User doesn't exists", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public void activate(String uuid){
        if (userRepository.existsByIdentifier(uuid)){
            User user = userRepository.findByIdentifier(uuid);
            user.setActivate(true);
            userRepository.save(user);
        } else {
            throw new CustomException("Identifier doesn't exists", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

}
