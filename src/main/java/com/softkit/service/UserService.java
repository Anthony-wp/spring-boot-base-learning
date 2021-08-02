package com.softkit.service;

import com.softkit.exception.CustomException;
import com.softkit.model.User;
import com.softkit.repository.UserRepository;
import com.softkit.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public String signin(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            return jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRoles());
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public String signup(User user) {
        if (!userRepository.existsByUsername(user.getUsername())) {
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

}
