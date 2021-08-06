package com.softkit.service;

import com.softkit.exception.CustomException;
import com.softkit.model.User;
import com.softkit.repository.UserRepository;
import com.softkit.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${images.path.string}")
    private String filePathToSaveUserImages;
    private final String baseUrl = "http://localhost:8080";
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public String signin(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            if (!userRepository.findByUsername(username).isActivate()){
                throw new CustomException("Account is not activated", HttpStatus.NOT_ACCEPTABLE);
            }
            return jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRoles());
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public String signup(User user) {
        if (!userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
            if (userRepository.existsByEmailIgnoreCase(user.getEmail())){
                throw new CustomException("Email is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            String uuid = UUID.randomUUID().toString();
            user.setActivationKey(uuid);
            user.setRegistrationDate(ZonedDateTime.now());
            String url = String.format("%s/users/activation?uuid=%s", baseUrl, uuid);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            emailService.sendMail(user.getEmail(), url);
            return jwtTokenProvider.createToken(user.getUsername(), user.getRoles());
        } else {
            throw new CustomException("Username is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public User whoami(HttpServletRequest req) {
        return userRepository.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
    }

    //  method must delete user, by username, throw appropriate exception is user doesn't exists
    @Transactional
    public void delete(String username) {
        if (userRepository.deleteByUsername(username) == 0){
            throw new CustomException("User doesn't exists", HttpStatus.NOT_FOUND);
        }
    }

    //  method must search user, by username, throw appropriate exception is user doesn't exists
    @Transactional
    public User search(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null){
            return user;
        } else {
            throw new CustomException("User doesn't exists", HttpStatus.NOT_FOUND);
        }
    }

//  method must create a new access token, similar to login
    @Transactional
    public String refresh(String username) {
        try{
            return jwtTokenProvider.createToken(username, userRepository.findByUsername(username).getRoles());
        } catch (NoSuchElementException e){
            throw new CustomException("User doesn't exists", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Transactional
    public void activate(String uuid){
        if (userRepository.existsByActivationKeyAndIsActivateFalse(uuid)) {
            User user = userRepository.findByActivationKey(uuid);
            user.setActivate(true);
            userRepository.save(user);
        } else {
            throw new CustomException("Identifier doesn't exists or is it already activated", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Transactional
    public void uploadImage(HttpServletRequest req, MultipartFile file) throws IOException {
        File uploadDir = new File(filePathToSaveUserImages);
        if (!new File(filePathToSaveUserImages).exists()){
            uploadDir.mkdir();
        }
        User user = userRepository.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
        String[] arr = file.getOriginalFilename().split("\\.(?=[^\\.]+$)");
        String fileName = String.format("%s_%d.%s", arr[0], user.getId(), arr[1]);
        File dest = new File(filePathToSaveUserImages + "/" + fileName);
        file.transferTo(dest);
        user.setUserAvatar(String.format("%s/users/images/%s", baseUrl, fileName));
        userRepository.save(user);
    }

    @Transactional
    public String loadImages(String username){
        return userRepository.findByUsername(username).getUserAvatar();
    }

    @Transactional
    public User updateUserData(HttpServletRequest req, String firstname, String lastname){
        User user = userRepository.findByUsername(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
        user.setFirstName(firstname);
        user.setLastName(lastname);
        userRepository.save(user);
        return user;
    }

    @Transactional
    public User updateUserDataForAdmin(String username, String firstname, String lastname){
        User user = userRepository.findByUsername(username);
        user.setFirstName(firstname);
        user.setLastName(lastname);
        userRepository.save(user);
        return user;
    }
}
